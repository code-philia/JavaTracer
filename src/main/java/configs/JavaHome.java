package configs;

import java.nio.file.Paths;

public record JavaHome(String path) {

    public static final String JAVA_BIN = "bin";
    public static final String JAVA = "java";
    public static final String JAVAC = "javac";

    /**
     * Check if this java home is valid. <br/> A valid java home should contain "bin/java" and
     * "bin/javac".
     *
     * @return True if this java home is valid, false otherwise
     */
    public boolean isValid() {
        if (this.path == null) {
            return false;
        }
        return Paths.get(this.path, JAVA_BIN, JAVA).toFile().exists()
            && Paths.get(this.path, JAVA_BIN, JAVAC).toFile().exists();
    }

    public String getExePath() {
        return Paths.get(this.path, JAVA_BIN, JAVA).toString();
    }

    public static JavaHome[] detectJavaHomes() {
        return null;
    }

    @Override
    public String toString() {
        return this.path;
    }


}
