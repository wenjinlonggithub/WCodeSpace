package com.architecture.designpattern.visitor;

import java.util.ArrayList;
import java.util.List;

public class VisitorExample {
    
    public interface Visitor {
        void visit(Document document);
        void visit(Image image);
        void visit(Video video);
    }
    
    public interface Element {
        void accept(Visitor visitor);
    }
    
    public static class Document implements Element {
        private String title;
        private String content;
        private int wordCount;
        
        public Document(String title, String content) {
            this.title = title;
            this.content = content;
            this.wordCount = content.split("\\s+").length;
        }
        
        @Override
        public void accept(Visitor visitor) {
            visitor.visit(this);
        }
        
        public String getTitle() {
            return title;
        }
        
        public String getContent() {
            return content;
        }
        
        public int getWordCount() {
            return wordCount;
        }
    }
    
    public static class Image implements Element {
        private String fileName;
        private String format;
        private double sizeInMB;
        
        public Image(String fileName, String format, double sizeInMB) {
            this.fileName = fileName;
            this.format = format;
            this.sizeInMB = sizeInMB;
        }
        
        @Override
        public void accept(Visitor visitor) {
            visitor.visit(this);
        }
        
        public String getFileName() {
            return fileName;
        }
        
        public String getFormat() {
            return format;
        }
        
        public double getSizeInMB() {
            return sizeInMB;
        }
    }
    
    public static class Video implements Element {
        private String fileName;
        private String format;
        private double sizeInMB;
        private int durationInSeconds;
        
        public Video(String fileName, String format, double sizeInMB, int durationInSeconds) {
            this.fileName = fileName;
            this.format = format;
            this.sizeInMB = sizeInMB;
            this.durationInSeconds = durationInSeconds;
        }
        
        @Override
        public void accept(Visitor visitor) {
            visitor.visit(this);
        }
        
        public String getFileName() {
            return fileName;
        }
        
        public String getFormat() {
            return format;
        }
        
        public double getSizeInMB() {
            return sizeInMB;
        }
        
        public int getDurationInSeconds() {
            return durationInSeconds;
        }
    }
    
    public static class SizeCalculatorVisitor implements Visitor {
        private double totalSize = 0;
        
        @Override
        public void visit(Document document) {
            double size = document.getWordCount() * 0.001;
            totalSize += size;
            System.out.println("Document '" + document.getTitle() + "': " + size + " MB");
        }
        
        @Override
        public void visit(Image image) {
            totalSize += image.getSizeInMB();
            System.out.println("Image '" + image.getFileName() + "': " + image.getSizeInMB() + " MB");
        }
        
        @Override
        public void visit(Video video) {
            totalSize += video.getSizeInMB();
            System.out.println("Video '" + video.getFileName() + "': " + video.getSizeInMB() + " MB");
        }
        
        public double getTotalSize() {
            return totalSize;
        }
    }
    
    public static class ExportVisitor implements Visitor {
        private List<String> exports = new ArrayList<>();
        
        @Override
        public void visit(Document document) {
            String export = "DOC: " + document.getTitle() + " (" + document.getWordCount() + " words)";
            exports.add(export);
            System.out.println("Exporting: " + export);
        }
        
        @Override
        public void visit(Image image) {
            String export = "IMG: " + image.getFileName() + " (" + image.getFormat() + ")";
            exports.add(export);
            System.out.println("Exporting: " + export);
        }
        
        @Override
        public void visit(Video video) {
            String export = "VID: " + video.getFileName() + " (" + 
                           video.getDurationInSeconds() + "s, " + video.getFormat() + ")";
            exports.add(export);
            System.out.println("Exporting: " + export);
        }
        
        public List<String> getExports() {
            return exports;
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Visitor Pattern Demo ===\n");
        
        List<Element> elements = new ArrayList<>();
        elements.add(new Document("Design Patterns Guide", "This is a comprehensive guide about design patterns in software engineering. It covers creational structural and behavioral patterns."));
        elements.add(new Image("diagram.png", "PNG", 2.5));
        elements.add(new Video("tutorial.mp4", "MP4", 150.0, 3600));
        elements.add(new Document("README", "Quick start guide for the project."));
        
        System.out.println("Size Calculation:");
        SizeCalculatorVisitor sizeVisitor = new SizeCalculatorVisitor();
        for (Element element : elements) {
            element.accept(sizeVisitor);
        }
        System.out.println("Total size: " + String.format("%.3f", sizeVisitor.getTotalSize()) + " MB\n");
        
        System.out.println("Export Operation:");
        ExportVisitor exportVisitor = new ExportVisitor();
        for (Element element : elements) {
            element.accept(exportVisitor);
        }
        System.out.println("\nTotal exported items: " + exportVisitor.getExports().size());
    }
}