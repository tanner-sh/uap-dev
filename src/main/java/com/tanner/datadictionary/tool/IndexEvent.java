package com.tanner.datadictionary.tool;

import org.openpdf.text.Anchor;
import org.openpdf.text.Document;
import org.openpdf.text.Element;
import org.openpdf.text.Phrase;
import org.openpdf.text.pdf.ColumnText;
import org.openpdf.text.pdf.PdfPageEventHelper;
import org.openpdf.text.pdf.PdfWriter;

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
