package com.coldmint.rust.pro.muqing;
import android.content.Context;
import android.os.Build;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

/** @noinspection unused, ResultOfMethodCallIgnored, ResultOfMethodCallIgnored */
public class wj {
    public static String filesdri;
    public wj(Context context) {
            wj.filesdri = Objects.requireNonNull(context.getExternalFilesDir("")).
                    getAbsolutePath() + "/";
//                context.getFilesDir().toString() + "/";
    }

    /*
     * 这里定义的是一个文件保存的方法，写入到文件中，所以是输出流
     * */
    public static boolean xrwb(String url, String text) {
        if (text == null) {
            text = "";
        }
        File file = new File(url);
//如果文件不存在，创建文件
        try {
            File parentFile = file.getParentFile();
            if (!Objects.requireNonNull(parentFile).isDirectory()) {
                parentFile.mkdirs();
            }
            if (!file.exists())
                file.createNewFile();
//创建FileOutputStream对象，写入内容
            FileOutputStream fos = new FileOutputStream(file);
//向文件中写入内容
            fos.write(text.getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String dqwb(String url) {
        try {
            File file = new File(url);
            if (!file.exists()) {
                return null;
            }
            FileInputStream fis = new FileInputStream(file);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            StringBuilder str = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                str.append(line);
            }
            br.close();
            fis.close();
            return str.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean cz(String url) {
        return new File(url).exists();
    }


    public static boolean sc(String url) {
        File file = new File(url);
        return file.delete();
    }

    public static boolean sc(File file, boolean bool) {
        if (!bool) {
            return file.delete();
        }
        if (file.exists()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File a : files) {
                    // 递归调用，删除子文件夹及其内容
                    // 删除文件
                    sc(a, a.isDirectory());
                }
            }
            return sc(file, false); // 删除当前文件夹
        }
        return false;
    }


    public String convertToMd5(String url) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(url.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte value : messageDigest) {
                String hex = Integer.toHexString(0xFF & value);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void fz(String sourceFilePath, String targetFilePath) {
        File sourceFile = new File(sourceFilePath);
        File targetFile = new File(targetFilePath);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try (InputStream in = Files.newInputStream(sourceFile.toPath());
                 OutputStream out = Files.newOutputStream(targetFile.toPath())) {
                byte[] buf = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buf)) > 0) {
                    out.write(buf, 0, bytesRead);
                }
                // 文件复制完成
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
