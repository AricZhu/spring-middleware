package com.aric.middleware;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class XmlReadDemo {
    public void testInputStream() {
        try (InputStream is = new FileInputStream("src/test/java/com/aric/middleware/hello.txt")) {
            int data = is.read();
            while (data != -1) {
                System.out.print((char) data);
                data = is.read();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void testInputStreamReader() {
        try (InputStream is = new FileInputStream("src/test/java/com/aric/middleware/hello.txt");
             Reader reader = new InputStreamReader(is);
        ) {
            int data = reader.read();
            while (data != -1) {
                System.out.print((char) data);
                data = reader.read();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        XmlReadDemo fileReaderDemo = new XmlReadDemo();
        fileReaderDemo.testInputStream();
        System.out.println("");
        fileReaderDemo.testInputStreamReader();
    }
}
