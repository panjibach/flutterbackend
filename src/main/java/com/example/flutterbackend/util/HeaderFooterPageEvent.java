package com.example.flutterbackend.util;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HeaderFooterPageEvent extends PdfPageEventHelper {
    private Font footerFont = new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC);

    public void onEndPage(PdfWriter writer, Document document) {
        // Footer Kiri: Tanggal Cetak
        ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER,
                new Phrase("Generated on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), footerFont),
                110, 30, 0);
        
        // Footer Kanan: Nomor Halaman
        ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER,
                new Phrase(String.format("Page %d", writer.getPageNumber()), footerFont),
                550, 30, 0);
    }
}