package com.medconnect.controller;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.medconnect.model.Prescription;
import com.medconnect.service.PrescriptionService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class PdfController {

    @Autowired
    private PrescriptionService prescriptionService;

    @GetMapping("/prescription/pdf/{id}")
    public void downloadPrescriptionPdf(
            @PathVariable Long id,
            HttpServletResponse response) throws Exception {

        Prescription prescription = prescriptionService
                .findById(id).orElse(null);

        if (prescription == null) {
            response.sendError(404, "Prescription not found");
            return;
        }

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition",
                "attachment; filename=prescription_" + id + ".pdf");

        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        // Colors
        BaseColor primaryColor = new BaseColor(26, 86, 219);
        BaseColor lightColor = new BaseColor(239, 246, 255);
        BaseColor greenColor = new BaseColor(16, 185, 129);

        // Fonts
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 22, Font.BOLD, primaryColor);
        Font subtitleFont = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL, BaseColor.GRAY);
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE);
        Font labelFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.DARK_GRAY);
        Font valueFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.BLACK);
        Font rxFont = new Font(Font.FontFamily.HELVETICA, 40, Font.BOLD, lightColor);

        // Header
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{3, 1});

        PdfPCell brandCell = new PdfPCell();
        brandCell.setBorder(Rectangle.NO_BORDER);
        brandCell.setPadding(10);
        Paragraph brandPara = new Paragraph();
        brandPara.add(new Chunk("MedConnect\n", titleFont));
        brandPara.add(new Chunk("Healthcare & Appointment System", subtitleFont));
        brandCell.addElement(brandPara);
        headerTable.addCell(brandCell);

        PdfPCell rxCell = new PdfPCell(new Phrase("Rx", rxFont));
        rxCell.setBorder(Rectangle.NO_BORDER);
        rxCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        rxCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        headerTable.addCell(rxCell);

        document.add(headerTable);

        // Blue divider line
        PdfPTable divider = new PdfPTable(1);
        divider.setWidthPercentage(100);
        divider.setSpacingBefore(5);
        divider.setSpacingAfter(15);
        PdfPCell dividerCell = new PdfPCell();
        dividerCell.setBackgroundColor(primaryColor);
        dividerCell.setFixedHeight(3);
        dividerCell.setBorder(Rectangle.NO_BORDER);
        divider.addCell(dividerCell);
        document.add(divider);

        // Doctor & Patient Info
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setSpacingAfter(15);

        // Doctor Info
        PdfPCell doctorCell = new PdfPCell();
        doctorCell.setBackgroundColor(new BaseColor(248, 250, 252));
        doctorCell.setBorder(Rectangle.BOX);
        doctorCell.setBorderColor(new BaseColor(229, 231, 235));
        doctorCell.setPadding(12);
        doctorCell.setBorderWidth(1);

        Paragraph doctorPara = new Paragraph();
        doctorPara.add(new Chunk("DOCTOR INFORMATION\n", new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, BaseColor.GRAY)));
        doctorPara.add(Chunk.NEWLINE);
        if (prescription.getDoctor() != null) {
            doctorPara.add(new Chunk("Dr. " + prescription.getDoctor().getUser().getName() + "\n",
                new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, primaryColor)));
            doctorPara.add(new Chunk(prescription.getDoctor().getSpecialization() + "\n", valueFont));
            doctorPara.add(new Chunk(prescription.getDoctor().getQualification(), valueFont));
        }
        doctorCell.addElement(doctorPara);
        infoTable.addCell(doctorCell);

        // Patient Info
        PdfPCell patientCell = new PdfPCell();
        patientCell.setBackgroundColor(new BaseColor(248, 250, 252));
        patientCell.setBorder(Rectangle.BOX);
        patientCell.setBorderColor(new BaseColor(229, 231, 235));
        patientCell.setPadding(12);
        patientCell.setBorderWidth(1);

        Paragraph patientPara = new Paragraph();
        patientPara.add(new Chunk("PATIENT INFORMATION\n", new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, BaseColor.GRAY)));
        patientPara.add(Chunk.NEWLINE);
        if (prescription.getPatient() != null) {
            patientPara.add(new Chunk(prescription.getPatient().getUser().getName() + "\n",
                new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, new BaseColor(16, 185, 129))));
            if (prescription.getPatient().getGender() != null)
                patientPara.add(new Chunk("Gender: " + prescription.getPatient().getGender() + "\n", valueFont));
            if (prescription.getPatient().getBloodGroup() != null)
                patientPara.add(new Chunk("Blood Group: " + prescription.getPatient().getBloodGroup(), valueFont));
        }
        patientCell.addElement(patientPara);
        infoTable.addCell(patientCell);

        document.add(infoTable);

        // Date
        Paragraph datePara = new Paragraph();
        datePara.add(new Chunk("Date: ", labelFont));
        datePara.add(new Chunk(prescription.getIssueDate().toString(), valueFont));
        datePara.setSpacingAfter(15);
        document.add(datePara);

        // Medicines Header
        PdfPTable medHeader = new PdfPTable(1);
        medHeader.setWidthPercentage(100);
        medHeader.setSpacingAfter(10);
        PdfPCell medHeaderCell = new PdfPCell(
            new Phrase("  PRESCRIBED MEDICINES", headerFont));
        medHeaderCell.setBackgroundColor(primaryColor);
        medHeaderCell.setPadding(10);
        medHeaderCell.setBorder(Rectangle.NO_BORDER);
        medHeader.addCell(medHeaderCell);
        document.add(medHeader);

        // Medicines Content
        PdfPTable medTable = new PdfPTable(1);
        medTable.setWidthPercentage(100);
        medTable.setSpacingAfter(15);
        PdfPCell medCell = new PdfPCell();
        medCell.setPadding(12);
        medCell.setBorder(Rectangle.BOX);
        medCell.setBorderColor(new BaseColor(229, 231, 235));
        medCell.addElement(new Paragraph(
            prescription.getMedicines() != null ? prescription.getMedicines() : "N/A", valueFont));
        medTable.addCell(medCell);
        document.add(medTable);

        // Dosage
        PdfPTable dosageHeader = new PdfPTable(1);
        dosageHeader.setWidthPercentage(100);
        dosageHeader.setSpacingAfter(10);
        PdfPCell dosageHeaderCell = new PdfPCell(
            new Phrase("  DOSAGE INSTRUCTIONS", headerFont));
        dosageHeaderCell.setBackgroundColor(greenColor);
        dosageHeaderCell.setPadding(10);
        dosageHeaderCell.setBorder(Rectangle.NO_BORDER);
        dosageHeader.addCell(dosageHeaderCell);
        document.add(dosageHeader);

        PdfPTable dosageTable = new PdfPTable(1);
        dosageTable.setWidthPercentage(100);
        dosageTable.setSpacingAfter(15);
        PdfPCell dosageCell = new PdfPCell();
        dosageCell.setPadding(12);
        dosageCell.setBorder(Rectangle.BOX);
        dosageCell.setBorderColor(new BaseColor(229, 231, 235));
        dosageCell.addElement(new Paragraph(
            prescription.getDosage() != null ? prescription.getDosage() : "N/A", valueFont));
        dosageTable.addCell(dosageCell);
        document.add(dosageTable);

        // Instructions
        if (prescription.getInstructions() != null
                && !prescription.getInstructions().isEmpty()) {
            PdfPTable instrHeader = new PdfPTable(1);
            instrHeader.setWidthPercentage(100);
            instrHeader.setSpacingAfter(10);
            PdfPCell instrHeaderCell = new PdfPCell(
                new Phrase("  SPECIAL INSTRUCTIONS", headerFont));
            instrHeaderCell.setBackgroundColor(new BaseColor(245, 158, 11));
            instrHeaderCell.setPadding(10);
            instrHeaderCell.setBorder(Rectangle.NO_BORDER);
            instrHeader.addCell(instrHeaderCell);
            document.add(instrHeader);

            PdfPTable instrTable = new PdfPTable(1);
            instrTable.setWidthPercentage(100);
            instrTable.setSpacingAfter(15);
            PdfPCell instrCell = new PdfPCell();
            instrCell.setPadding(12);
            instrCell.setBorder(Rectangle.BOX);
            instrCell.setBorderColor(new BaseColor(229, 231, 235));
            instrCell.addElement(new Paragraph(
                prescription.getInstructions(), valueFont));
            instrTable.addCell(instrCell);
            document.add(instrTable);
        }

        // Footer
        PdfPTable footer = new PdfPTable(1);
        footer.setWidthPercentage(100);
        footer.setSpacingBefore(20);
        PdfPCell footerCell = new PdfPCell();
        footerCell.setBackgroundColor(new BaseColor(248, 250, 252));
        footerCell.setBorder(Rectangle.BOX);
        footerCell.setBorderColor(new BaseColor(229, 231, 235));
        footerCell.setPadding(12);
        Paragraph footerPara = new Paragraph();
        footerPara.add(new Chunk("This is a digitally generated prescription from MedConnect.\n",
            new Font(Font.FontFamily.HELVETICA, 9, Font.ITALIC, BaseColor.GRAY)));
        footerPara.add(new Chunk("For any queries, please contact your doctor.",
            new Font(Font.FontFamily.HELVETICA, 9, Font.ITALIC, BaseColor.GRAY)));
        footerCell.addElement(footerPara);
        footer.addCell(footerCell);
        document.add(footer);

        document.close();
    }
}