package com.officecollab.documentcore.service;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Maps a file extension to ONLYOFFICE's documentType ("word"/"cell"/"slide") and to the MIME type stored on
 * newly-created documents. Extension is taken from the stored filename (the last path segment of storage_key),
 * which is more reliable for this than trusting an uploader-supplied Content-Type header.
 */
public final class OnlyOfficeDocumentTypes {

    private static final Set<String> WORD_EXTENSIONS = Set.of("docx", "doc", "pdf", "txt", "odt", "rtf");
    private static final Set<String> CELL_EXTENSIONS = Set.of("xlsx", "xls", "csv", "ods");
    private static final Set<String> SLIDE_EXTENSIONS = Set.of("pptx", "ppt", "odp");

    private static final Map<String, String> MIME_TYPES = Map.of(
            "docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation"
    );

    private OnlyOfficeDocumentTypes() {
    }

    public static String extensionOf(String filenameOrKey) {
        int dot = filenameOrKey.lastIndexOf('.');
        if (dot < 0 || dot == filenameOrKey.length() - 1) {
            return "";
        }
        return filenameOrKey.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    /** ONLYOFFICE editorConfig documentType: "word" | "cell" | "slide". */
    public static String documentTypeFor(String extension) {
        String ext = extension.toLowerCase(Locale.ROOT);
        if (WORD_EXTENSIONS.contains(ext)) {
            return "word";
        }
        if (CELL_EXTENSIONS.contains(ext)) {
            return "cell";
        }
        if (SLIDE_EXTENSIONS.contains(ext)) {
            return "slide";
        }
        throw new IllegalArgumentException("Unsupported file extension for ONLYOFFICE: " + extension);
    }

    public static String mimeTypeForTemplateExtension(String extension) {
        String mime = MIME_TYPES.get(extension.toLowerCase(Locale.ROOT));
        if (mime == null) {
            throw new IllegalArgumentException("Unsupported template extension: " + extension);
        }
        return mime;
    }
}
