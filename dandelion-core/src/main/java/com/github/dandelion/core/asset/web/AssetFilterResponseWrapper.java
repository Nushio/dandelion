package com.github.dandelion.core.asset.web;

import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class AssetFilterResponseWrapper extends HttpServletResponseWrapper {
    protected PrintWriter printWriter = null;
    protected CharArrayWriter writer = null;

    public AssetFilterResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        throw new IllegalStateException("getWriter() has already been called for this response");
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (writer == null) {
            writer = new CharArrayWriter();
        }

        if (printWriter == null) {
            printWriter = new PrintWriter(writer);
        }

        return printWriter;
    }

    public String getWrappedContent() {
        return writer.toString();
    }
}