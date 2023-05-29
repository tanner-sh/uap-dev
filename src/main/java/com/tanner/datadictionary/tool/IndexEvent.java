package com.tanner.datadictionary.tool;

import com.itextpdf.text.Anchor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

public class IndexEvent extends PdfPageEventHelper {

    private int page;
    private boolean body;

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
        if (body) {
            page++;
            //设置页脚页码
            float x = (document.rightMargin() + document.right() + document.leftMargin() - document.left()) / 2.0F + 20F;
            Anchor anchor = new Anchor("" + page);
            anchor.setName("user");
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, new Phrase(anchor),
                    x, document.bottom() - 20, 0);
        }
    }

    public boolean isBody() {
        return body;
    }

    public void setBody(boolean body) {
        this.body = body;
    }
}
