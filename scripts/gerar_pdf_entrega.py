#!/usr/bin/env python3
# -*- coding: utf-8 -*-

from reportlab.lib.pagesizes import letter
from reportlab.lib import colors
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, HRFlowable
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
import sys

def gerar_pdf(caminho_saida="dimdim_webapp.pdf", video_url="https://youtu.be/SEU_VIDEO_AQUI"):
    doc = SimpleDocTemplate(
        caminho_saida,
        pagesize=letter,
        rightMargin=54,
        leftMargin=54,
        topMargin=54,
        bottomMargin=54
    )

    styles = getSampleStyleSheet()
    
    # Custom styles
    primary_color = colors.HexColor("#0078D4")
    dark_color = colors.HexColor("#0F172A")
    gray_color = colors.HexColor("#475569")
    card_bg = colors.HexColor("#F8FAFC")
    border_color = colors.HexColor("#E2E8F0")

    title_style = ParagraphStyle(
        'DocTitle',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=22,
        leading=26,
        textColor=primary_color,
        alignment=0,
        spaceAfter=6
    )

    subtitle_style = ParagraphStyle(
        'DocSub',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=12,
        leading=16,
        textColor=gray_color,
        alignment=0,
        spaceAfter=15
    )

    section_heading = ParagraphStyle(
        'SectionHeading',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=13,
        leading=17,
        textColor=dark_color,
        spaceBefore=14,
        spaceAfter=8
    )

    body_style = ParagraphStyle(
        'Body',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=10,
        leading=14,
        textColor=dark_color
    )

    body_bold = ParagraphStyle(
        'BodyBold',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=10,
        leading=14,
        textColor=dark_color
    )

    link_style = ParagraphStyle(
        'LinkStyle',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=10,
        leading=14,
        textColor=primary_color
    )

    story = []

    # Conteúdo restrito ao exigido pelo professor: grupo, integrantes/RM, GitHub e vídeo ("mais nada").
    story.append(Paragraph("DimDim — Entrega Web App", title_style))
    story.append(Spacer(1, 6))

    # 1. Nome do Grupo
    story.append(Paragraph("1. Nome do Grupo", section_heading))
    grupo_table = Table([[Paragraph("<b>Grupo:</b> DimDim", body_style)]], colWidths=[500])
    grupo_table.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,-1), card_bg),
        ('BOX', (0,0), (-1,-1), 1, border_color),
        ('PADDING', (0,0), (-1,-1), 10),
    ]))
    story.append(grupo_table)

    story.append(Spacer(1, 12))

    # 2. Integrantes e RMs
    story.append(Paragraph("2. Integrantes da Equipe e RMs", section_heading))
    integrantes_data = [
        [Paragraph("<b>Nome do Aluno</b>", body_bold), Paragraph("<b>RM</b>", body_bold)],
        [Paragraph("Gabriel Maciel Alves de Oliveira", body_style), Paragraph("RM562795", body_style)],
        [Paragraph("Vitória Rodrigues Martins", body_style), Paragraph("RM565160", body_style)],
        [Paragraph("Augusto Bonomo Júnior", body_style), Paragraph("RM565155", body_style)],
        [Paragraph("Thomas Fontes", body_style), Paragraph("RM562254", body_style)],
        [Paragraph("Matheus Pereira Molina", body_style), Paragraph("RM563399", body_style)],
    ]
    int_table = Table(integrantes_data, colWidths=[360, 140])
    int_table.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), colors.HexColor("#EEF2F6")),
        ('GRID', (0,0), (-1,-1), 0.5, border_color),
        ('PADDING', (0,0), (-1,-1), 7),
        ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, card_bg])
    ]))
    story.append(int_table)

    story.append(Spacer(1, 14))

    # 3. Links Obrigatórios
    story.append(Paragraph("3. Links", section_heading))
    
    github_url = "https://github.com/Gabriel-Maciel06/CPDevopsDInDIn.git"
    links_data = [
        [
            Paragraph("<b>Link do Repositório GitHub:</b>", body_style),
            Paragraph(f'<a href="{github_url}">{github_url}</a>', link_style)
        ],
        [
            Paragraph("<b>Link do Vídeo Demonstrativo:</b>", body_style),
            Paragraph(f'<a href="{video_url}">{video_url}</a>', link_style)
        ]
    ]
    links_table = Table(links_data, colWidths=[180, 320])
    links_table.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,-1), card_bg),
        ('BOX', (0,0), (-1,-1), 1, border_color),
        ('INNERGRID', (0,0), (-1,-1), 0.5, border_color),
        ('PADDING', (0,0), (-1,-1), 10),
    ]))
    story.append(links_table)

    doc.build(story)
    print(f"[+] PDF gerado com sucesso em: {caminho_saida}")

if __name__ == "__main__":
    caminho = sys.argv[1] if len(sys.argv) > 1 else "dimdim_webapp.pdf"
    video = sys.argv[2] if len(sys.argv) > 2 else "https://youtu.be/SEU_VIDEO_AQUI"
    gerar_pdf(caminho, video)
