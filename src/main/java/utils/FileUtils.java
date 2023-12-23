package utils;

import core.Constants;
import core.Pair;
import core.SavRtException;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author LLT
 */
public class FileUtils {

  private static final String FILE_IDX_START_CH = "_";
  private static final int FILE_SEQ_START_IDX = 0;

  private FileUtils() {}

  public static File createTempFolder(String folderName) {
    File folder = getFileInTempFolder(folderName);
    if (folder.exists()) {
      if (folder.isDirectory()) {
        return folder;
      }
      throw new SavRtException(String.format("Cannot create temp folder: %s", folderName));
    }
    folder.mkdirs();
    return folder;
  }

  public static File createFolder(String folderPath) {
    File folder = new File(folderPath);
    if (folder.exists()) {
      if (folder.isDirectory()) {
        return folder;
      }
      throw new SavRtException(String.format("Path %s is not a folder!", folderPath));
    }
    folder.mkdirs();
    return folder;
  }

  public static File getFileInTempFolder(String fileName) {
    File tmpdir = new File(System.getProperty("java.io.tmpdir"));
    File file = new File(tmpdir, fileName);
    return file;
  }

  public static void appendFile(String fileName, String content) {
    writeFile(fileName, content, true);
  }

  public static void writeFile(String fileName, String content) {
    writeFile(fileName, content, false);
  }

  public static void writeFile(String fileName, String content, boolean append) {
    File file = getFileCreateIfNotExist(fileName);
    FileOutputStream stream;
    try {
      stream = new FileOutputStream(file, append);
      stream.write(content.getBytes());
      stream.close();
    } catch (IOException e) {
      throw new SavRtException(e);
    }
  }

  public static File getFileCreateIfNotExist(String path) {
    File file = new File(path);
    if (!file.exists()) {
      File folder = file.getParentFile();
      if (!folder.exists()) {
        folder.mkdirs();
      }
      try {
        file.createNewFile();
      } catch (IOException e) {
        throw new SavRtException(e);
      }
    }
    return file;
  }

  public static String getFilePath(String... fragments) {
    return StringUtils.join(Arrays.asList(fragments), Constants.FILE_SEPARATOR);
  }

  public static String getFilePath(List<String> fragments) {
    return StringUtils.join(fragments, Constants.FILE_SEPARATOR);
  }

  public static String copyFileToFolder(
      String sourceFile, String destFolder, boolean preserveFileDate) {
    sourceFile = sourceFile.replace(Constants.FILE_SEPARATOR, "/");
    String fileName = sourceFile.substring(sourceFile.lastIndexOf("/") + 1);
    String destFile = getFilePath(destFolder, fileName);
    try {
      org.apache.commons.io.FileUtils.copyFile(
          new File(sourceFile), new File(destFile), preserveFileDate);
    } catch (IOException e) {
      throw new SavRtException(e);
    }
    return destFile;
  }

  public static void copyFile(String srcFile, String destFile, boolean preserveFileDate) {
    try {
      org.apache.commons.io.FileUtils.copyFile(
          new File(srcFile), new File(destFile), preserveFileDate);
    } catch (IOException e) {
      throw new SavRtException(e);
    }
  }

  public static String backupFile(String fileName) {
    int idx = fileName.lastIndexOf(".");
    if (idx < 0) {
      return fileName + "_bk";
    }
    String newfile =
        fileName.substring(0, idx)
            + "_bk"
            + fileName.substring(idx);
    copyFile(fileName, newfile, true);
    return newfile;
  }

  public static void deleteFolder(File file) {
    if (!file.exists()) {
      return;
    }
    if (!file.isDirectory()) {
      if (file.getAbsolutePath().length() > 260) {
        File newFile = new File(FileUtils.getFilePath(file.getParent(), "$"));
        file.renameTo(newFile);
        newFile.delete();
      } else {
        file.delete();
      }
    } else {
      for (File sub : file.listFiles()) {
        deleteFolder(sub);
      }
    }
    file.delete();
  }

  public static void deleteAllFiles(String folderPath) {
    deleteAllFiles(folderPath, null);
  }

  public static void deleteAllFiles(String folderPath, FilenameFilter filter) {
    File folder = new File(folderPath);
    if (!folder.exists() || !folder.isDirectory()) {
      return;
    }
    File[] files;
    if (filter != null) {
      files = folder.listFiles(filter);
    } else {
      files = folder.listFiles();
    }
    if (!CollectionUtils.isEmpty(files)) {
      deleteFiles(Arrays.asList(files));
    }
  }

  public static void deleteFiles(List<File> files) {
    for (File file : CollectionUtils.nullToEmpty(files)) {
      if (file.getAbsolutePath().length() > 260) {
        File newFile = new File(file.getAbsolutePath().substring(0, 250));
        file.renameTo(newFile);
        newFile.delete();
      } else {
        file.delete();
      }
    }
  }

  public static File createNewFileInSeq(
      String dir, final String filePrefix, final String fileSuffix) {
    int fileIdx = FILE_SEQ_START_IDX;
    Pair<File, Integer> lastFile = getLastFile(dir, filePrefix, fileSuffix);
    if (lastFile != null) {
      fileIdx = lastFile.second() + 1;
    }
    String filepath =
        dir
            + File.separator
            + filePrefix
            + FILE_IDX_START_CH
            + fileIdx
            + fileSuffix;
    return getFileCreateIfNotExist(filepath);
  }

  public static Pair<File, Integer> getLastFile(
      String dir, final String filePrefix, final String fileSuffix) {
    File folder = new File(dir);
    if (!folder.exists()) {
      return null;
    }
    File[] files =
        folder.listFiles(
            new FilenameFilter() {
              @Override
              public boolean accept(File folder, String name) {
                return name.endsWith(fileSuffix) && name.startsWith(filePrefix);
              }
            });
    if (CollectionUtils.isEmpty(files)) {
      return null;
    }
    return getLastFile(files, fileSuffix);
  }

  public static Pair<File, Integer> getLastFile(File[] files, String suffix) {
    int lastIdx = -1;
    File lastFile = null;
    for (File file : files) {
      String fileName = file.getName();
      String fileIdxStr =
          fileName.substring(fileName.lastIndexOf(FILE_IDX_START_CH) + 1, fileName.indexOf(suffix));
      int fileIdx = lastIdx;
      try {
        fileIdx = Integer.valueOf(fileIdxStr);
      } catch (Exception e) {
        fileIdx = lastIdx;
      }
      if (fileIdx > lastIdx) {
        lastIdx = fileIdx;
        lastFile = file;
      }
    }
    if (lastIdx < 0) {
      return null;
    }
    return Pair.of(lastFile, lastIdx);
  }

  public static File getFileEndWith(String folderPath, final String fileSuffix) {
    File folder = new File(folderPath);
    File[] matches =
        folder.listFiles(
            new FilenameFilter() {

              @Override
              public boolean accept(File dir, String name) {
                  return name.endsWith(fileSuffix);
              }
            });
    if (CollectionUtils.isEmpty(matches)) {
      return null;
    }
    return matches[0];
  }

  public static String lookupFile(String path, final String fileName) {
    File file = new File(path);
    if (file.isFile()) {
      return lookupFile(file.getParent(), fileName);
    } else {
      String[] matchedFiles = file.list((dir, name) -> name.equals(fileName));
      if (CollectionUtils.isNotEmpty(matchedFiles)) {
        return matchedFiles[0];
      }
      for (File sub : file.listFiles()) {
        if (sub.isDirectory()) {
          String match = lookupFile(sub.getAbsolutePath(), fileName);
          if (match != null) {
            return match;
          }
        }
      }
      return null;
    }
  }

//  public static List<String> readLines(String filePath) {
//    if (filePath == null) {
//      return null;
//    }
//    File file = new File(filePath);
//    if (!file.exists()) {
//      return null;
//    }
//    List<String> lines = new ArrayList<>();
//    List<Closeable> closables = new ArrayList<>();
//    try {
//      FileInputStream stream = new FileInputStream(file);
//      closables.add(stream);
//      BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
//      closables.add(reader);
//      String line = reader.readLine();
//      while (line != null) {
//        lines.add(line);
//        line = reader.readLine();
//      }
//      return lines;
//    } catch (Exception e) {
//      AgentLogger.info("Read file error: " + e.getMessage());
//      AgentLogger.error(e);
//      return null;
//    } finally {
//      for (Closeable closeble : closables) {
//        try {
//          closeble.close();
//        } catch (IOException e) {
//          // ignore
//        }
//      }
//    }
//  }

  public static List<String> readLines(Reader input) throws IOException {
    BufferedReader reader = new BufferedReader(input);
    List<String> list = new ArrayList<>();
    String line = reader.readLine();
    while (line != null) {
      list.add(line);
      line = reader.readLine();
    }
    return list;
  }


  public static int copy(InputStream input, OutputStream output) throws IOException {
    byte[] buffer = new byte[1024 * 4];
    long count = 0;
    int n = 0;
    while (-1 != (n = input.read(buffer))) {
      output.write(buffer, 0, n);
      count += n;
    }
    if (count > Integer.MAX_VALUE) {
      return -1;
    }
    return (int) count;
  }

  public static void closeStreams(Closeable... closables) {
    for (Closeable closable : closables) {
      if (closable != null) {
        try {
          closable.close();
        } catch (IOException e) {
          // ignore
        }
      }
    }
  }
}
