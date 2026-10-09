package com.hospital.util;

import com.hospital.config.AppConfig;
import com.hospital.model.Invoice;
import com.hospital.model.InvoiceItem;
import com.hospital.model.Prescription;
import com.hospital.model.PrescriptionItem;
import com.hospital.model.LabOrder;
import com.hospital.model.LabResult;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;

/**
 * Generates professional PDF documents for Invoices, Prescriptions, and Lab Reports.
 */
public final class PdfGenerator {

    private static final Logger logger = LoggerFactory.getLogger(PdfGenerator.class);

    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(24, 43, 73));
    private static final Font SUBTITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY);
    private static final Font SECTION_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(24, 43, 73));
    private static final Font BODY_FONT = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
    private static final Font BOLD_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.BLACK);
    private static final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);

    private PdfGenerator() {}

    public static void generateInvoicePdf(Invoice invoice, File outputFile) throws Exception {
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        PdfWriter.getInstance(document, new FileOutputStream(outputFile));
        document.open();

        // Hospital Header
        addHeader(document, "PATIENT INVOICE & RECEIPT");

        // Invoice & Patient Metadata Table
        PdfPTable metaTable = new PdfPTable(2);
        metaTable.setWidthPercentage(100);
        metaTable.setSpacingBefore(15);
        metaTable.setSpacingAfter(15);

        PdfPCell c1 = new PdfPCell();
        c1.setBorder(Rectangle.NO_BORDER);
        c1.addElement(new Paragraph("Invoice Number: " + invoice.getInvoiceNumber(), BOLD_FONT));
        c1.addElement(new Paragraph("Issue Date: " + DateUtil.formatDate(invoice.getIssueDate()), BODY_FONT));
        c1.addElement(new Paragraph("Payment Status: " + invoice.getPaymentStatus(), BOLD_FONT));
        if (invoice.getAdmissionNumber() != null) {
            c1.addElement(new Paragraph("Admission: " + invoice.getAdmissionNumber(), BODY_FONT));
        }

        PdfPCell c2 = new PdfPCell();
        c2.setBorder(Rectangle.NO_BORDER);
        c2.addElement(new Paragraph("Patient Name: " + invoice.getPatientName(), BOLD_FONT));
        c2.addElement(new Paragraph("Patient Code: " + invoice.getPatientCode(), BODY_FONT));
        if (invoice.getPatientPhone() != null) {
            c2.addElement(new Paragraph("Phone: " + invoice.getPatientPhone(), BODY_FONT));
        }

        metaTable.addCell(c1);
        metaTable.addCell(c2);
        document.add(metaTable);

        // Itemized Table
        PdfPTable table = new PdfPTable(new float[]{3.5f, 1.5f, 1.0f, 2.0f, 2.0f});
        table.setWidthPercentage(100);

        addTableHeader(table, new String[]{"Description", "Type", "Qty", "Unit Price", "Total"});

        for (InvoiceItem item : invoice.getItems()) {
            table.addCell(createCell(item.getDescription(), Element.ALIGN_LEFT));
            table.addCell(createCell(item.getItemType(), Element.ALIGN_CENTER));
            table.addCell(createCell(String.valueOf(item.getQuantity()), Element.ALIGN_CENTER));
            table.addCell(createCell(CurrencyUtil.format(item.getUnitPrice()), Element.ALIGN_RIGHT));
            table.addCell(createCell(CurrencyUtil.format(item.getTotalPrice()), Element.ALIGN_RIGHT));
        }
        document.add(table);

        // Summary Totals Table
        PdfPTable totalsTable = new PdfPTable(2);
        totalsTable.setWidthPercentage(40);
        totalsTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalsTable.setSpacingBefore(10);

        addTotalRow(totalsTable, "Subtotal:", CurrencyUtil.format(invoice.getSubtotal()), false);
        if (invoice.getDiscountAmount().doubleValue() > 0) {
            addTotalRow(totalsTable, "Discount (" + invoice.getDiscountPercent() + "%):", "-" + CurrencyUtil.format(invoice.getDiscountAmount()), false);
        }
        if (invoice.getTaxAmount().doubleValue() > 0) {
            addTotalRow(totalsTable, "Tax (" + invoice.getTaxPercent() + "%):", "+" + CurrencyUtil.format(invoice.getTaxAmount()), false);
        }
        addTotalRow(totalsTable, "Total Amount:", CurrencyUtil.format(invoice.getTotalAmount()), true);
        addTotalRow(totalsTable, "Paid Amount:", CurrencyUtil.format(invoice.getPaidAmount()), false);
        addTotalRow(totalsTable, "Balance Due:", CurrencyUtil.format(invoice.getBalanceAmount()), true);

        document.add(totalsTable);

        // Footer Note
        Paragraph footer = new Paragraph("\n\nThank you for choosing " + AppConfig.getHospitalName() + ". For queries, contact billing at " + AppConfig.getHospitalPhone(), SUBTITLE_FONT);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);

        document.close();
        logger.info("Invoice PDF generated at {}", outputFile.getAbsolutePath());
    }

    public static void generatePrescriptionPdf(Prescription prescription, File outputFile) throws Exception {
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        PdfWriter.getInstance(document, new FileOutputStream(outputFile));
        document.open();

        addHeader(document, "MEDICAL PRESCRIPTION");

        // Doctor & Patient details
        PdfPTable meta = new PdfPTable(2);
        meta.setWidthPercentage(100);
        meta.setSpacingBefore(15);
        meta.setSpacingAfter(15);

        PdfPCell c1 = new PdfPCell();
        c1.setBorder(Rectangle.NO_BORDER);
        c1.addElement(new Paragraph("Prescription: " + prescription.getPrescriptionNumber(), BOLD_FONT));
        c1.addElement(new Paragraph("Prescription Date: " + DateUtil.formatDate(prescription.getPrescriptionDate()), BODY_FONT));
        c1.addElement(new Paragraph("Prescribing Doctor: " + prescription.getDoctorName(), BOLD_FONT));

        PdfPCell c2 = new PdfPCell();
        c2.setBorder(Rectangle.NO_BORDER);
        c2.addElement(new Paragraph("Patient: " + prescription.getPatientName(), BOLD_FONT));
        c2.addElement(new Paragraph("Patient Code: " + prescription.getPatientCode(), BODY_FONT));
        if (prescription.getDiagnosis() != null) {
            c2.addElement(new Paragraph("Diagnosis: " + prescription.getDiagnosis(), BODY_FONT));
        }

        meta.addCell(c1);
        meta.addCell(c2);
        document.add(meta);

        // Medicines Table
        PdfPTable table = new PdfPTable(new float[]{3.0f, 1.5f, 1.5f, 1.5f, 1.0f, 2.5f});
        table.setWidthPercentage(100);
        addTableHeader(table, new String[]{"Medicine", "Dosage", "Frequency", "Duration", "Qty", "Instructions"});

        for (PrescriptionItem it : prescription.getItems()) {
            table.addCell(createCell(it.getMedicineName(), Element.ALIGN_LEFT));
            table.addCell(createCell(it.getDosage(), Element.ALIGN_CENTER));
            table.addCell(createCell(it.getFrequency(), Element.ALIGN_CENTER));
            table.addCell(createCell(it.getDuration(), Element.ALIGN_CENTER));
            table.addCell(createCell(String.valueOf(it.getQuantity()), Element.ALIGN_CENTER));
            table.addCell(createCell(it.getInstructions() != null ? it.getInstructions() : "-", Element.ALIGN_LEFT));
        }
        document.add(table);

        if (prescription.getNotes() != null && !prescription.getNotes().trim().isEmpty()) {
            Paragraph notes = new Paragraph("\nDoctor's Advice & Notes:\n" + prescription.getNotes(), BODY_FONT);
            document.add(notes);
        }

        Paragraph sig = new Paragraph("\n\n\n_______________________________\nDoctor's Signature & Seal", BOLD_FONT);
        sig.setAlignment(Element.ALIGN_RIGHT);
        document.add(sig);

        document.close();
        logger.info("Prescription PDF generated at {}", outputFile.getAbsolutePath());
    }

    public static void generateLabReportPdf(LabOrder order, File outputFile) throws Exception {
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        PdfWriter.getInstance(document, new FileOutputStream(outputFile));
        document.open();

        addHeader(document, "DIAGNOSTIC LABORATORY REPORT");

        PdfPTable meta = new PdfPTable(2);
        meta.setWidthPercentage(100);
        meta.setSpacingBefore(15);
        meta.setSpacingAfter(15);

        PdfPCell c1 = new PdfPCell();
        c1.setBorder(Rectangle.NO_BORDER);
        c1.addElement(new Paragraph("Order Number: " + order.getOrderNumber(), BOLD_FONT));
        c1.addElement(new Paragraph("Order Date: " + DateUtil.formatDateTime(order.getOrderDate()), BODY_FONT));
        c1.addElement(new Paragraph("Ordering Doctor: " + order.getDoctorName(), BODY_FONT));
        c1.addElement(new Paragraph("Status: " + order.getStatus(), BOLD_FONT));

        PdfPCell c2 = new PdfPCell();
        c2.setBorder(Rectangle.NO_BORDER);
        c2.addElement(new Paragraph("Patient: " + order.getPatientName(), BOLD_FONT));
        c2.addElement(new Paragraph("Patient Code: " + order.getPatientCode(), BODY_FONT));
        if (order.getClinicalNotes() != null) {
            c2.addElement(new Paragraph("Clinical Notes: " + order.getClinicalNotes(), BODY_FONT));
        }

        meta.addCell(c1);
        meta.addCell(c2);
        document.add(meta);

        // Results table
        PdfPTable table = new PdfPTable(new float[]{3.0f, 2.0f, 2.0f, 1.5f, 2.5f});
        table.setWidthPercentage(100);
        addTableHeader(table, new String[]{"Test Name", "Result Value", "Reference Range", "Unit", "Remarks"});

        for (LabResult r : order.getResults()) {
            table.addCell(createCell(r.getTestName(), Element.ALIGN_LEFT));
            PdfPCell resCell = createCell(r.getResultValue() + (r.isAbnormal() ? " *" : ""), Element.ALIGN_CENTER);
            if (r.isAbnormal()) {
                resCell.setBackgroundColor(new Color(255, 230, 230));
            }
            table.addCell(resCell);
            table.addCell(createCell(r.getReferenceRange() != null ? r.getReferenceRange() : "-", Element.ALIGN_CENTER));
            table.addCell(createCell(r.getUnit() != null ? r.getUnit() : "", Element.ALIGN_CENTER));
            table.addCell(createCell(r.getRemarks() != null ? r.getRemarks() : "", Element.ALIGN_LEFT));
        }
        document.add(table);

        Paragraph notes = new Paragraph("\n* Denotes result outside normal biological reference interval.", SUBTITLE_FONT);
        document.add(notes);

        Paragraph sig = new Paragraph("\n\n\n_______________________________\nAuthorized Laboratory Signatory", BOLD_FONT);
        sig.setAlignment(Element.ALIGN_RIGHT);
        document.add(sig);

        document.close();
        logger.info("Lab Report PDF generated at {}", outputFile.getAbsolutePath());
    }

    private static void addHeader(Document document, String documentTitle) throws DocumentException {
        Paragraph name = new Paragraph(AppConfig.getHospitalName(), TITLE_FONT);
        name.setAlignment(Element.ALIGN_CENTER);
        document.add(name);

        Paragraph tagline = new Paragraph(AppConfig.getHospitalTagline() + "\n" + AppConfig.getHospitalAddress() + " | Tel: " + AppConfig.getHospitalPhone(), SUBTITLE_FONT);
        tagline.setAlignment(Element.ALIGN_CENTER);
        document.add(tagline);

        Paragraph title = new Paragraph("\n" + documentTitle, SECTION_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
    }

    private static void addTableHeader(PdfPTable table, String[] headers) {
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, HEADER_FONT));
            cell.setBackgroundColor(new Color(30, 60, 102));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(6);
            table.addCell(cell);
        }
    }

    private static PdfPCell createCell(String text, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", BODY_FONT));
        cell.setHorizontalAlignment(alignment);
        cell.setPadding(5);
        return cell;
    }

    private static void addTotalRow(PdfPTable table, String label, String value, boolean bold) {
        Font f = bold ? BOLD_FONT : BODY_FONT;
        PdfPCell lCell = new PdfPCell(new Phrase(label, f));
        lCell.setBorder(Rectangle.NO_BORDER);
        lCell.setHorizontalAlignment(Element.ALIGN_LEFT);

        PdfPCell vCell = new PdfPCell(new Phrase(value, f));
        vCell.setBorder(Rectangle.NO_BORDER);
        vCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

        table.addCell(lCell);
        table.addCell(vCell);
    }
}
