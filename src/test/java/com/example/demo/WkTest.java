package com.example.demo;

public class WkTest {
    public static void main(String[] args) {
        String cmd="d:/javasoft/wkhtmltopdf/bin/wkhtmltoimage --quality 75  https://www.nowcoder.com d:/work/data/wk-image/1.png";
        try {
            Runtime.getRuntime().exec(cmd);
            System.out.println("ok.");
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
