package com.example.bnk.service.product;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import com.example.bnk.dto.product.ProductTermsImagesDto;
import com.example.bnk.dto.product.ProductTermsImagesDto.TermsFile;
import com.example.bnk.dto.product.ProductTermsImagesDto.TermsPage;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductTermsPdfService {

	private static final float RENDER_DPI = 120f;

	private final ResourceLoader resourceLoader;

	public ProductTermsImagesDto getTermsImages(long productNo) {
		Path termsDirectory = resolveTermsDirectory(productNo);

		if (!Files.isDirectory(termsDirectory)) {
			return ProductTermsImagesDto.builder()
					.productNo(productNo)
					.terms(List.of())
					.build();
		}

		List<TermsFile> termsFiles;

		try (var paths = Files.list(termsDirectory)) {
			termsFiles = paths
					.filter(Files::isRegularFile)
					.filter(this::isPdf)
					.sorted(Comparator.comparing(path -> path.getFileName().toString()))
					.map(this::renderPdf)
					.toList();
		} catch (IOException e) {
			throw new IllegalStateException("약관 PDF 목록을 읽을 수 없습니다.", e);
		}

		return ProductTermsImagesDto.builder()
				.productNo(productNo)
				.terms(termsFiles)
				.build();
	}

	public List<Path> listTermsPdfFiles(long productNo) {
		Path termsDirectory = resolveTermsDirectory(productNo);

		if (!Files.isDirectory(termsDirectory)) {
			return List.of();
		}

		try (var paths = Files.list(termsDirectory)) {
			return paths
					.filter(Files::isRegularFile)
					.filter(this::isPdf)
					.sorted(Comparator.comparing(path -> path.getFileName().toString()))
					.toList();
		} catch (IOException e) {
			throw new IllegalStateException("약관 PDF 목록을 읽을 수 없습니다.", e);
		}
	}

	private Path resolveTermsDirectory(long productNo) {
		try {
			Resource resource = resourceLoader.getResource("classpath:static/terms/product/" + productNo);

			if (resource.exists()) {
				return resource.getFile().toPath();
			}
		} catch (IOException ignored) {
			// Fall through to the source-tree path used during local development.
		}

		return Path.of("src", "main", "resources", "static", "terms", "product", String.valueOf(productNo));
	}

	private boolean isPdf(Path path) {
		return path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".pdf");
	}

	private TermsFile renderPdf(Path pdfPath) {
		try (PDDocument document = PDDocument.load(pdfPath.toFile())) {
			PDFRenderer renderer = new PDFRenderer(document);
			List<TermsPage> pages = new java.util.ArrayList<>();

			for (int i = 0; i < document.getNumberOfPages(); i++) {
				BufferedImage image = renderer.renderImageWithDPI(i, RENDER_DPI, ImageType.RGB);
				pages.add(TermsPage.builder()
						.pageNo(i + 1)
						.imageBase64(encodePng(image))
						.build());
			}

			String fileName = pdfPath.getFileName().toString();

			return TermsFile.builder()
					.termsTitle(removePdfExtension(fileName))
					.fileName(fileName)
					.pageCount(document.getNumberOfPages())
					.pages(pages)
					.build();
		} catch (IOException e) {
			throw new IllegalStateException("약관 PDF를 이미지로 변환할 수 없습니다: " + pdfPath.getFileName(), e);
		}
	}

	private String encodePng(BufferedImage image) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		ImageIO.write(image, "png", outputStream);
		return Base64.getEncoder().encodeToString(outputStream.toByteArray());
	}

	private String removePdfExtension(String fileName) {
		return fileName.replaceFirst("(?i)\\.pdf$", "");
	}
}
