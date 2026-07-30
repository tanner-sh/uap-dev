package com.tanner.datadictionary.tool;

import org.openpdf.text.Anchor;
import org.openpdf.text.Chapter;
import org.openpdf.text.Chunk;
import org.openpdf.text.Document;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.BaseFont;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import org.openpdf.text.pdf.draw.DottedLineSeparator;
import com.tanner.datadictionary.entity.AggTable;
import com.tanner.datadictionary.entity.ColumnInfo;
import com.tanner.datadictionary.entity.TableInfo;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.awt.Color;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PdfBuilder implements IExportBuilder {

    private static final String FONT_RESOURCE = "/fonts/NotoSansSC-VF.ttf";

    @Override
    public void build(List<AggTable> aggTableList, String exportDirPath) throws Exception {
        //输出文件地址
        String filePath = Path.of(exportDirPath, "datadictionary.pdf").toString();
        BaseFont bfChinese = loadChineseBaseFont();
        Font font = new Font(bfChinese, 12, Font.BOLDITALIC);
        // 设置类型，加粗
        font.setStyle(Font.NORMAL);
        Font cnFont = getChineseFontAsStyle(bfChinese, 16);
        //页面大小
        Rectangle rect = new Rectangle(PageSize.A4).rotate();
        //页面背景色
        rect.setBackgroundColor(new Color(0xFF, 0xFF, 0xDE));
        //设置边框颜色
        rect.setBorderColor(new Color(0xFF, 0xFF, 0xDE));
        Document doc = new Document(rect);
        PdfWriter contentWriter = PdfWriter.getInstance(doc, new ByteArrayOutputStream());
        //设置事件
        ContentEvent event = new ContentEvent();
        contentWriter.setPageEvent(event);
        //存目录监听 开始
        doc.open();
        int order = 1;
        List<Chapter> chapterList = new ArrayList<>();
        //根据chapter章节分页
        //表格
        //设置表格模板
        String[] tableHeader = {"序列", "列名", "类型", "可空", "默认值", "注释", "枚举"};
        for (AggTable aggTable : aggTableList) {
            TableInfo tableInfo = aggTable.getTableInfo();
            Chapter chapter = new Chapter(new Paragraph(tableInfo.getTableName()), order);
            //设置跳转地址
            Phrase point = new Paragraph("基本信息:", cnFont);
            Anchor tome = new Anchor(point);
            tome.setName(tableInfo.getTableName());
            Phrase comment = new Phrase(" " + tableInfo.getComment() + "\n\n",
                    getChineseFontAsStyle(bfChinese, 16));
            //组装基本数据
            Paragraph contentInfo = new Paragraph();
            contentInfo.add(tome);
            contentInfo.add(comment);
            chapter.add(contentInfo);
            chapter.add(new Paragraph(""));
            //组装表格
            Paragraph tableParagraph = new Paragraph();
            //设置表格
            PdfPTable table = setTableHeader(tableHeader,
                    getChineseFontAsStyle(bfChinese, 16));
            //设置列信息
            setTableColumn(table, aggTable, font);
            tableParagraph.add(table);
            chapter.add(tableParagraph);
            //加入文档中
            doc.add(chapter);
            //保存章节内容
            chapterList.add(chapter);
            order++;
        }
        doc.close();
        //存目录监听 结束
        Document document = new Document(rect);
        IndexEvent indexEvent = new IndexEvent();
        try (FileOutputStream output = new FileOutputStream(filePath)) {
            PdfWriter writer = PdfWriter.getInstance(document, output);
            writer.setPageEvent(indexEvent);
            document.open();
        //添加章节目录
        Chapter indexChapter = new Chapter(
                new Paragraph("", getFontAsStyle(bfChinese)), 0);
        indexChapter.setNumberDepth(-1);
        // 设置数字深度
        int i = 1;
        for (Map.Entry<String, Integer> index : event.index.entrySet()) {
            String key = index.getKey();
            String[] keyValue = key.split(" ");
            //设置跳转显示名称
            int pageNo = index.getValue();
            Chunk pointChunk = new Chunk(new DottedLineSeparator());
            Chunk pageNoChunk = new Chunk(String.valueOf(pageNo));
            String tempDescription = key;
            if (!StringUtils.isEmpty(aggTableList.get(i - 1).getTableInfo().getComment())) {
                tempDescription += "(" + aggTableList.get(i - 1).getTableInfo().getComment() + ")";
            }
            Paragraph jumpParagraph = new Paragraph(tempDescription,
                    getChineseFontAsStyle(bfChinese, 12));
            jumpParagraph.add(pointChunk);
            jumpParagraph.add(pageNoChunk);
            Anchor anchor = new Anchor(jumpParagraph);
            String jump = keyValue[keyValue.length - 1].trim();
            //设置跳转链接
            anchor.setReference("#" + jump);
            indexChapter.add(anchor);
            indexChapter.add(new Paragraph());
            i++;
        }
        document.add(indexChapter);
        document.newPage();
        //添加内容
        for (Chapter c : chapterList) {
            indexEvent.setBody(true);
            document.add(c);
        }
            document.close();
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }
    }

    private BaseFont loadChineseBaseFont() throws Exception {
        try (InputStream input = PdfBuilder.class.getResourceAsStream(FONT_RESOURCE)) {
            if (input == null) {
                throw new IllegalStateException("找不到内置中文字体: " + FONT_RESOURCE);
            }
            BaseFont baseFont = BaseFont.createFont(
                    "NotoSansSC-VF.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED,
                    true, input.readAllBytes(), null);
            baseFont.setSubset(true);
            return baseFont;
        }
    }

    private Font getChineseFontAsStyle(BaseFont baseFont, float size) {
        Font font = new Font(baseFont, size, Font.NORMAL);
        font.setColor(Color.BLACK);
        return font;
    }

    private Font getFontAsStyle(BaseFont baseFont) {
        Font font = new Font(baseFont);
        font.setColor(Color.BLACK);
        font.setSize(18);
        return font;
    }

    private PdfPTable setTableHeader(String[] header, Font font) {
        int columnSize = header.length;
        PdfPTable table = new PdfPTable(columnSize);
        table.setWidthPercentage(100);
        for (String s : header) {
            PdfPCell pdfPCell = new PdfPCell(new Paragraph(s, font));
            pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
            pdfPCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            pdfPCell.setBackgroundColor(Color.LIGHT_GRAY);
            table.addCell(pdfPCell);
        }
        return table;
    }

    private void setTableColumn(PdfPTable table, AggTable aggTable, Font font) {
        List<ColumnInfo> Columns = aggTable.getColumnInfoList();
        for (ColumnInfo column : Columns) {
            addCell(table, String.valueOf(column.getColumnId()), font);
            addCell(table, column.getColumnName(), font);
            addCell(table, column.getType(), font);
            addCell(table, column.getNullAble(), font);
            addCell(table, column.getDefaultValue(), font);
            addCell(table, column.getComment(), font);
            addCell(table, column.getEnumValue(), font);
        }
    }

    private void addCell(PdfPTable table, String value, Font font) {
        PdfPCell cell = new PdfPCell();
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.addElement(new Paragraph(Objects.toString(value, ""), font));
        table.addCell(cell);
    }

}
