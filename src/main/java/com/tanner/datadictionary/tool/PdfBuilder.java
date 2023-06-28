package com.tanner.datadictionary.tool;

import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.DottedLineSeparator;
import com.openhtmltopdf.css.parser.property.PageSize;
import com.tanner.datadictionary.entity.AggTable;
import com.tanner.datadictionary.entity.ColumnInfo;
import com.tanner.datadictionary.entity.TableInfo;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PdfBuilder implements IExportBuilder {

    @Override
    public void build(List<AggTable> aggTableList, String exportDirPath) throws Exception {
        //输出文件地址
        String filePath = Path.of(exportDirPath, "datadictionary.pdf").toString();
        BaseFont bfChinese = BaseFont.createFont("STSongStd-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
        Font font = new Font(bfChinese, 12, Font.BOLDITALIC);
        // 设置类型，加粗
        font.setStyle(Font.NORMAL);
        Font cnFont = getChineseFontAsStyle(16);
        //页面大小
        Rectangle rect = new Rectangle(PageSize.A4).rotate();
        //页面背景色
        rect.setBackgroundColor(new BaseColor(0xFF, 0xFF, 0xDE));
        //设置边框颜色
        rect.setBorderColor(new BaseColor(0xFF, 0xFF, 0xDE));
        Document doc = new Document(rect);
        PdfWriter contentWriter = PdfWriter.getInstance(doc, new ByteArrayOutputStream());
        //设置事件
        ContentEvent event = new ContentEvent();
        contentWriter.setPageEvent(event);
        //存目录监听 开始
        doc.open();
        int order = 1;
        List<Chapter> chapterList = new ArrayList<Chapter>();
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
            Phrase comment = new Phrase(" " + tableInfo.getComment() + "\n\n", getChineseFontAsStyle(16));
            //组装基本数据
            Paragraph contentInfo = new Paragraph();
            contentInfo.add(tome);
            contentInfo.add(comment);
            chapter.add(contentInfo);
            chapter.add(new Paragraph(""));
            //组装表格
            Paragraph tableParagraph = new Paragraph();
            //设置表格
            PdfPTable table = setTableHeader(tableHeader, getChineseFontAsStyle(16));
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
        PdfWriter writer = PdfWriter.getInstance(document, new ByteArrayOutputStream());
        IndexEvent indexEvent = new IndexEvent();
        writer.setPageEvent(indexEvent);
        PdfWriter.getInstance(document, new FileOutputStream(filePath));
        document.open();
        //添加章节目录
        Chapter indexChapter = new Chapter(new Paragraph("", getFontAsStyle()), 0);
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
            Paragraph jumpParagraph = new Paragraph(tempDescription, getChineseFontAsStyle(12));
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
    }

    private Font getChineseFontAsStyle(float size) {
        try {
            //中文字体
            BaseFont bfChinese = BaseFont.createFont("STSongStd-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            Font font = new Font(bfChinese, size, Font.NORMAL);
            font.setColor(BaseColor.BLACK);
            return font;
        } catch (Exception e) {
            return new Font();
        }
    }

    private Font getFontAsStyle() {
        Font font = new Font();
        font.setColor(BaseColor.BLACK);
        font.setSize((float) 18);
        return font;
    }

    private PdfPTable setTableHeader(String[] header, Font font) {
        int columnSize = header.length;
        PdfPTable table = new PdfPTable(columnSize);
        table.setWidthPercentage(100);
        for (int i = 0; i < columnSize; i++) {
            PdfPCell pdfPCell = new PdfPCell(new Paragraph(header[i], font));
            pdfPCell.setVerticalAlignment(Element.ALIGN_CENTER);
            pdfPCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            pdfPCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            table.addCell(pdfPCell);
        }
        return table;
    }

    private void setTableColumn(PdfPTable table, AggTable aggTable, Font font) throws IllegalAccessException {
        List<ColumnInfo> Columns = aggTable.getColumnInfoList();
        for (ColumnInfo column : Columns) {
            table = buildCell(column, table, font);
        }
    }

    private PdfPTable buildCell(ColumnInfo columnInfo, PdfPTable pdfPTable, Font font) throws IllegalAccessException {
        Font cnFont = getChineseFontAsStyle(12);
        Field[] fields = columnInfo.getClass().getDeclaredFields();
        for (int j = 0; j < fields.length; j++) {
            PdfPCell cell = new PdfPCell();
            cell.setVerticalAlignment(Element.ALIGN_CENTER);
            //将设置私有构造器设为可取值
            fields[j].setAccessible(true);
            // 得到类型和名字取值
            Paragraph paragraph = new Paragraph(ObjectUtils.toString(fields[j].get(columnInfo), ""), font);
            //添加到表格
            cell.addElement(paragraph);
            pdfPTable.addCell(cell);
        }
        return pdfPTable;
    }

    private boolean isChineseContent(String content) {
        String regex = "[一-龥]";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return true;
        }
        return false;
    }

}
