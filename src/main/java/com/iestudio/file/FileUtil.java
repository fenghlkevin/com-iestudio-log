package com.iestudio.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.helpers.LogLog;

import com.iestudio.object.ObjUtil;

/**
 * @author <p>
 *         Title: 文件函数
 *         </p>
 *         <p>
 *         Description:
 *         </p>
 *         该类主要提供了文件操作的各种功能，包括文件创建、读取，解压缩等<br>
 */
public class FileUtil {

    /**
     * 得到文件的URL
     * 
     * @param fileName
     *            文件名
     * @return 文件的URL
     */
    public static URL getFileURL(String fileName) {
        ClassLoader loader = FileUtil.class.getClassLoader();
        URL fileUrl = loader.getResource(fileName);
        return fileUrl;
    }

    /**
     * load filename
     * 
     * @throws IOException
     */
    public static Properties load(String filename, String charset) throws IOException {
        InputStream is = null;
        Reader reader = null;
        try {
            URL url = FileUtil.getFileURL(filename);
            Properties prop = new Properties();
            if (url != null) {
                is = url.openStream();
            } else {
                is = new FileInputStream(filename);
            }
            reader = new BufferedReader(new InputStreamReader(is, charset));
            //prop.load(reader);
            return prop;
        } finally {
            if (is != null) {
                is.close();
            }
            if (reader != null) {
                reader.close();
            }

        }
    }

    /**
     * Read contents of file into byte array.
     * 
     * @param path
     *            file path
     * @return array of bytes containing all data from file
     * @throws IOException
     *             on file access error
     */
    public static byte[] getFileBytes(String path) {
        File file = new File(path);
        int length = (int) file.length();
        byte[] data = new byte[length];
        FileInputStream in;
        try {
            in = new FileInputStream(file);
            int offset = 0;
            do {
                offset += in.read(data, offset, length - offset);
            } while (offset < data.length);
            in.close();
            return data;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    /**
     * 创建文件
     * 
     * @param filepath
     *            文件名
     * @param data
     *            数据
     * @throws IOException
     */
    public static void CreateFile(String filepath, byte[] data) throws IOException {

        // 创建文件夹
        FileUtil.CreateDirs(FileUtil.getPath(filepath));

        FileOutputStream out = new FileOutputStream(filepath);

        out.write(data);

        out.close();

    }

    /**
     * 根据完整的文件名得到该文件路径
     * 
     * @param filepath
     * @return
     */
    public static String getPath(String filepath) {

        if (ObjUtil.isEmpty(filepath))
            return null;

        return filepath.substring(0, filepath.lastIndexOf('/'));
    }

    /**
     * 创建文件
     * 
     * @param filepath
     *            文件名
     * @param data
     *            数据
     * @param encoding
     *            编码格式
     * @throws IOException
     */
    public static void CreateFile(String filepath, String data, String encoding) throws IOException {

        // 创建文件夹
        FileUtil.CreateDirs(FileUtil.getPath(filepath));

        FileOutputStream out = new FileOutputStream(filepath);
        OutputStreamWriter writer = new OutputStreamWriter(out, encoding);
        BufferedWriter bwriter = new BufferedWriter(writer);

        bwriter.write(data);

        bwriter.close();
        writer.close();
        out.close();

    }

    /**
     * 创建文件夹
     * 
     * @param path
     *            文件夹名称
     * @return
     */
    public static boolean CreateDirs(String path) {

        File objIniFile = new File(path);
        if (objIniFile.exists() == false) {
            return objIniFile.mkdirs();
        }
        return true;
    }

    public static String getType(String filename) {
        if (ObjUtil.isEmpty(filename)) {
            return null;
        }
        if (filename.indexOf('.') == -1) {
            return null;
        }

        return filename.substring(filename.lastIndexOf('.') + 1);
    }

    public static void recurAllFolders(File root, List<File> handlerFiles) {
        // log1.debug("获取目录下所有文件信息");
        for (File file : root.listFiles()) {
            if (file.isDirectory()) {
                recurAllFolders(file, handlerFiles);
            } else {
                handlerFiles.add(file);
            }
        }
    }

    public static void recurAllFolders(File root, List<File> handlerFiles, FileFilter filter) {
        // log1.debug("获取目录下所有文件信息");
        for (File file : root.listFiles()) {
            if (file.isDirectory()) {
                if (filter == null) {
                    recurAllFolders(file, handlerFiles);
                } else {
                    recurAllFolders(file, handlerFiles, filter);
                }

            } else {
                if (filter.accept(file)) {
                    handlerFiles.add(file);
                }

            }
        }
    }

    public static void recurAllFolders(File root, List<File> handlerFiles, FileFilter filter, int catalogLevel) {
        for (File file : root.listFiles()) {
            if (file.isDirectory()) {
                if (filter == null) {
                    recurAllFolders(file, handlerFiles);
                } else {
                    recurAllFolders(file, handlerFiles, filter);
                }

            } else {
                if (filter.accept(file)) {
                    handlerFiles.add(file);
                }

            }
        }
    }

    public static void copyFiles(String originalDataPath, String targetDataPath, FileFilter filter) {
        if (ObjUtil.isEmpty(originalDataPath) || ObjUtil.isEmpty(targetDataPath)) {
            throw new RuntimeException("文件夹名称错误，不能进行操作");
        }
        File root = new File(originalDataPath);
        if (!root.exists()) {
            throw new RuntimeException("源文件夹 [" + originalDataPath + "]不存在，不能进行操作");
        }
        File target = new File(targetDataPath);
        if (!target.exists()) {
            target.mkdirs();
            //throw new RuntimeException("目标文件夹 [" + targetDataPath + "] 不存在，不能进行操作");
        }
        List<File> list = new ArrayList<File>();
        recurAllFolders(root, list);
        for (File oneFile : list) {
            if (!oneFile.exists()) {
                continue;
            }
            if (filter != null && !filter.accept(oneFile)) {
                continue;
            }
            LogLog.debug(FileUtil.class.getName() + " copyFiles 开始Copy文件：" + oneFile.getName());

            try {
                moveFileToDirectory(oneFile, new File(targetDataPath), true, false, true);
            } catch (IOException e) {
                LogLog.error("Copy文件[" + oneFile.getName() + "] 到 [" + targetDataPath + "]失败", e);
            }
        }

    }

    /**
     * <p>
     * Discription:[移动文件，可删除长度是零的文件，可根据过滤条件进行文件过滤]
     * </p>
     * 
     * @param originalDataPath
     * @param targetDataPath
     * @param extend
     * @author:[创建者中文名字]
     * @update:[日期YYYY-MM-DD] [更改人姓名][变更描述]
     */
    public static void moveFilesAndDelete(String originalDataPath, String targetDataPath, FileFilter filter, boolean deleteZeroFile, boolean append2DestFile) {
        if (ObjUtil.isEmpty(originalDataPath) || ObjUtil.isEmpty(targetDataPath)) {
            throw new RuntimeException("文件夹名称错误，不能进行操作");
        }
        File root = new File(originalDataPath);
        if (!root.exists()) {
            throw new RuntimeException("源文件夹 [" + originalDataPath + "]不存在，不能进行操作");
        }
        File target = new File(targetDataPath);
        if (!target.exists()) {
            throw new RuntimeException("目标文件夹 [" + targetDataPath + "] 不存在，不能进行操作");
        }
        List<File> list = new ArrayList<File>();
        recurAllFolders(root, list);
        for (File oneFile : list) {
            if (!oneFile.exists()) {
                continue;
            }
            if (filter != null && !filter.accept(oneFile)) {
                continue;
            }
            LogLog.debug(FileUtil.class.getName() + " moveFilesAndDelete 开始移动 并删除文件：" + oneFile.getName());
            if (oneFile.length() <= 0) {
                if (deleteZeroFile) {
                    oneFile.delete();
                }
            } else {
                try {
                    moveFileToDirectory(oneFile, new File(targetDataPath), true, append2DestFile);
                } catch (IOException e) {
                    LogLog.error("移动文件[" + oneFile.getName() + "] 到 [" + targetDataPath + "]失败", e);
                }
            }
        }
    }

    public static void deleteFiles(String originalDataPath, FileFilter filter) {
        if (ObjUtil.isEmpty(originalDataPath)) {
            throw new RuntimeException("文件夹名称错误，不能进行操作");
        }
        File root = new File(originalDataPath);
        if (!root.exists()) {
            throw new RuntimeException("源文件夹不存在，不能进行操作");
        }
        List<File> list = new ArrayList<File>();
        recurAllFolders(root, list);
        for (File oneFile : list) {
            if (!oneFile.exists()) {
                continue;
            }
            if (filter != null && !filter.accept(oneFile)) {
                continue;
            }
            oneFile.delete();
        }
    }

    public static String getOneFileName(String moveToPath, int i) {
        File temp = new File(moveToPath);
        if (temp.exists()) {
            moveToPath = moveToPath.replaceAll(".bak" + (i - 1), "");
            return getOneFileName(moveToPath + ".bak" + i, i + 1);
        }
        return moveToPath;
    }

    // =============================从org.apache.commons.io.FileUtils中粘出代码======================

    /**
     * Moves a file to a directory.
     * 
     * @param srcFile
     *            the file to be moved
     * @param destDir
     *            the destination file
     * @param createDestDir
     *            If <code>true</code> create the destination directory,
     *            otherwise if <code>false</code> throw an IOException
     * @throws NullPointerException
     *             if source or destination is <code>null</code>
     * @throws IOException
     *             if source or destination is invalid
     * @throws IOException
     *             if an IO error occurs moving the file
     * @since Commons IO 1.4
     */
    public static void moveFileToDirectory(File srcFile, File destDir, boolean createDestDir, boolean appendToDestFile) throws IOException {
        moveFileToDirectory(srcFile, destDir, createDestDir, appendToDestFile, false);
    }

    public static void moveFileToDirectory(File srcFile, File destDir, boolean createDestDir, boolean appendToDestFile, boolean doCopy) throws IOException {
        if (srcFile == null) {
            throw new NullPointerException("Source must not be null");
        }
        if (destDir == null) {
            throw new NullPointerException("Destination directory must not be null");
        }
        if (!destDir.exists() && createDestDir) {
            destDir.mkdirs();
        }
        if (!destDir.exists()) {
            throw new FileNotFoundException("Destination directory '" + destDir + "' does not exist [createDestDir=" + createDestDir + "]");
        }
        if (!destDir.isDirectory()) {
            throw new IOException("Destination '" + destDir + "' is not a directory");
        }
        moveFile(srcFile, new File(destDir, srcFile.getName()), appendToDestFile, doCopy);
    }

    /**
     * Moves a file.
     * <p>
     * When the destination file is on another file system, do a
     * "copy and delete".
     * 
     * @param srcFile
     *            the file to be moved
     * @param destFile
     *            the destination file
     * @throws NullPointerException
     *             if source or destination is <code>null</code>
     * @throws IOException
     *             if source or destination is invalid
     * @throws IOException
     *             if an IO error occurs moving the file
     * @since Commons IO 1.4
     */
    public static void moveFile(File srcFile, File destFile, boolean appendToDestFile) throws IOException {
        moveFile(srcFile, destFile, appendToDestFile, false);
    }

    public static void moveFile(File srcFile, File destFile, boolean appendToDestFile, boolean doCopy) throws IOException {
        if (srcFile == null) {
            throw new NullPointerException("Source must not be null");
        }
        if (destFile == null) {
            throw new NullPointerException("Destination must not be null");
        }
        if (!srcFile.exists()) {
            throw new FileNotFoundException("Source '" + srcFile + "' does not exist");
        }
        if (srcFile.isDirectory()) {
            throw new IOException("Source '" + srcFile + "' is a directory");
        }
        if (!appendToDestFile && destFile.exists()) {
            throw new IOException("Destination '" + destFile + "' already exists");
        }
        if (destFile.isDirectory()) {
            throw new IOException("Destination '" + destFile + "' is a directory");
        }
        if (doCopy) {
            doCopyFile(srcFile, destFile, true, appendToDestFile);
        } else {
            boolean rename = srcFile.renameTo(destFile);
            if (!rename) {
                doCopyFile(srcFile, destFile, true, appendToDestFile);
                if (!srcFile.delete()) {
                    FileUtils.deleteQuietly(destFile);
                    throw new IOException("Failed to delete original file '" + srcFile + "' after copy to '" + destFile + "'");
                }
            }
        }
    }

    /**
     * Internal copy file method.
     * 
     * @param srcFile
     *            the validated source file, must not be <code>null</code>
     * @param destFile
     *            the validated destination file, must not be <code>null</code>
     * @param preserveFileDate
     *            whether to preserve the file date
     * @throws IOException
     *             if an error occurs
     */
    private static void doCopyFile(File srcFile, File destFile, boolean preserveFileDate, boolean appendToDestFile) throws IOException {
        if (destFile.exists() && destFile.isDirectory()) {
            throw new IOException("Destination '" + destFile + "' exists but is a directory");
        }

        FileInputStream input = new FileInputStream(srcFile);
        try {
            FileOutputStream output = new FileOutputStream(destFile, appendToDestFile);
            try {
                IOUtils.copy(input, output);
            } finally {
                IOUtils.closeQuietly(output);
            }
        } finally {
            IOUtils.closeQuietly(input);
        }

        if (!appendToDestFile && srcFile.length() != destFile.length()) {
            throw new IOException("Failed to copy full contents from '" + srcFile + "' to '" + destFile + "'");
        }
        if (preserveFileDate) {
            destFile.setLastModified(srcFile.lastModified());
        }
    }

    /**
     * 返回某目录最新的文件
     * @param directory
     * @return
     * @throws IOException
     */
    public static File getNewestFile(File directory) throws IOException {
        if (!directory.exists() || !directory.isDirectory()) {
            throw new IOException("Destination '" + directory + "' not exists or is not a directory");
        }

        File[] files = directory.listFiles();

        if (files == null || files.length <= 0) {
            return null;
        }

        Comparator<File> compare = new Comparator<File>() {

            public int compare(File o1, File o2) {
                Long s1 = new Long(o1.lastModified());
                Long s2 = new Long(o2.lastModified());
                return s1.compareTo(s2);
            }
        };

        Arrays.sort(files, compare);

        return files[0];

    }

}
