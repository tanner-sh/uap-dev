package com.tanner.datadictionary.tool;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

import java.util.LinkedHashMap;
import java.util.Map;

public class ContentEvent extends PdfPageEventHelper {

    public Map<String, Integer> index = new LinkedHashMap<>();
    private int page;

    @Override
    public void onStartPage(PdfWriter writer, Document document) {
        page++;
    }

    @Override
    public void onChapter(PdfWriter writer, Document document, float paragraphPosition, Paragraph title) {
        index.put(title.getContent(), page);
    }

    @Override
    public void onSection(PdfWriter writer, Document document, float paragraphPosition, int depth, Paragraph title) {
        onChapter(writer, document, paragraphPosition, title);
    }

}
