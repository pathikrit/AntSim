package ants.core;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtils {
    public static byte[] read(File file) {
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] ret = read(fileInputStream);
            fileInputStream.close();
            return ret;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public static byte[] read(InputStream is) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        transfer(is, bos);

        return bos.toByteArray();
    }

    public static void transfer(InputStream is, File targetFile, byte[] buf) {
        try {
            FileOutputStream fos = new FileOutputStream(targetFile);
            transfer(is, fos, buf);
            fos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void transfer(File subFile, OutputStream outputStream, byte[] buf) {
        try {
            transfer(new FileInputStream(subFile), outputStream, buf);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static void transfer(InputStream is, OutputStream os) {
        transfer(is, os, new byte[1024]);
    }

    private static void transfer(InputStream is, OutputStream os, byte[] buf) {
        try {
            while (true) {
                int read = is.read(buf);
                if (read < 0) {
                    break;
                }
                os.write(buf, 0, read);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void delete(File file) {
        if (file == null) {
            throw new NullArgumentException("file");
        }
        if (!file.exists()) {
            throw new IllegalArgumentException("File does not exist: " + file);
        }
        if (file.isDirectory()) {
            throw new IllegalArgumentException("Use deleteDirectory() to delete a directory. " + file);
        }
        if (!file.delete())
            throw new RuntimeException("Could not delete: " + file);
    }

    public static void deleteDirectory(File file) {
        if (file == null) {
            throw new NullArgumentException("file");
        }
        if (!file.exists()) {
            throw new IllegalArgumentException("Directory does not exist: " + file);
        }
        if (!file.isDirectory()) {
            throw new IllegalArgumentException("Not a directory.");
        }
        recursiveDelete(file);
    }

    private static void recursiveDelete(File file) {
        for (File child : file.listFiles()) {
            if (child.isDirectory()) {
                recursiveDelete(child);
            } else if (!child.delete()) {
                throw new RuntimeException("Could not delete: " + child);
            }
        }

        if (!file.delete())
            throw new RuntimeException("Could not delete: " + file);
    }

    public static void mkdirs(File file) {
        if (!file.mkdirs())
            throw new RuntimeException("Could not mkdirs: " + file);
    }

    // ERROR //
    public static void copy(File from, File to) {
        // Byte code:
        //   0: aconst_null
        //   1: astore_2
        //   2: aconst_null
        //   3: astore_3
        //   4: new 16	java/io/FileInputStream
        //   7: dup
        //   8: aload_0
        //   9: invokespecial 18	java/io/FileInputStream:<init>	(Ljava/io/File;)V
        //   12: astore_2
        //   13: new 59	java/io/FileOutputStream
        //   16: dup
        //   17: aload_1
        //   18: invokespecial 61	java/io/FileOutputStream:<init>	(Ljava/io/File;)V
        //   21: astore_3
        //   22: sipush 1024
        //   25: newarray byte
        //   27: astore 4
        //   29: aload_2
        //   30: aload 4
        //   32: invokevirtual 81	java/io/InputStream:read	([B)I
        //   35: istore 5
        //   37: iload 5
        //   39: iconst_m1
        //   40: if_icmpne +6 -> 46
        //   43: goto +63 -> 106
        //   46: aload_3
        //   47: aload 4
        //   49: iconst_0
        //   50: iload 5
        //   52: invokevirtual 86	java/io/OutputStream:write	([BII)V
        //   55: goto -26 -> 29
        //   58: astore 4
        //   60: new 27	java/lang/RuntimeException
        //   63: dup
        //   64: aload 4
        //   66: invokespecial 29	java/lang/RuntimeException:<init>	(Ljava/lang/Throwable;)V
        //   69: athrow
        //   70: astore 6
        //   72: aload_2
        //   73: ifnull +7 -> 80
        //   76: aload_2
        //   77: invokevirtual 155	java/io/InputStream:close	()V
        //   80: aload_3
        //   81: ifnull +22 -> 103
        //   84: aload_3
        //   85: invokevirtual 156	java/io/OutputStream:close	()V
        //   88: goto +15 -> 103
        //   91: astore 7
        //   93: new 27	java/lang/RuntimeException
        //   96: dup
        //   97: aload 7
        //   99: invokespecial 29	java/lang/RuntimeException:<init>	(Ljava/lang/Throwable;)V
        //   102: athrow
        //   103: aload 6
        //   105: athrow
        //   106: aload_2
        //   107: ifnull +7 -> 114
        //   110: aload_2
        //   111: invokevirtual 155	java/io/InputStream:close	()V
        //   114: aload_3
        //   115: ifnull +22 -> 137
        //   118: aload_3
        //   119: invokevirtual 156	java/io/OutputStream:close	()V
        //   122: goto +15 -> 137
        //   125: astore 7
        //   127: new 27	java/lang/RuntimeException
        //   130: dup
        //   131: aload 7
        //   133: invokespecial 29	java/lang/RuntimeException:<init>	(Ljava/lang/Throwable;)V
        //   136: athrow
        //   137: return
        //
        // Exception table:
        //   from	to	target	type
        //   4	58	58	java/lang/Exception
        //   4	70	70	finally
        //   72	88	91	java/lang/Exception
        //   106	122	125	java/lang/Exception
    }
}
