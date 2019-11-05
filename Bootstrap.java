/********************************************************************************
 * Copyright (c) 2011-2017 Red Hat Inc. and/or its affiliates and others
 *
 * This program and the accompanying materials are made available under the 
 * terms of the Apache License, Version 2.0 which is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: Apache-2.0 
 ********************************************************************************/
package org.eclipse.ceylon.launcher;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.ceylon.common.Constants;
import org.eclipse.ceylon.common.Versions;

/**
 * This is the earliest bootstrap class for the Ceylon tool chain.
 * It does nothing more than trying to locate the system repository
 * and load an appropriate ceylon.bootstrap module.
 * Appropriate in this case means it will try to find the version this
 * class was compiled with (see <code>Versions.CEYLON_VERSION_NUMBER</code>)
 * or the version specified by the <code>CEYLON_VERSION</code> environment
 * variable.
 * After it locates the module it will pass the execution on to the
 * <code>Launcher.main()</code> it contains.
 * 
 * IMPORTANT This class should contain as little logic as possible and
 * delegate as soon as it can to the <code>Launcher</code> in the
 * ceylon.bootstrap module. This way we can maintain backward and forward
 * compatibility as much as possible.
 * 
 * @author Tako Schotanus
 */
public class Bootstrap {

    public static final String CEYLON_DOWNLOAD_BASE_URL = "https://ceylon-lang.org/download/dist/";
    
    public static final String FILE_BOOTSTRAP_PROPERTIES = "ceylon-bootstrap.properties";
    public static final String FILE_BOOTSTRAP_JAR = "ceylon-bootstrap.jar";
    public static final String FILE_BS_ORIGIN = "BS_ORIGIN";
    
    public static final String KEY_SHA256SUM = "sha256sum";
    public static final String KEY_INSTALLATION = "installation";
    public static final String KEY_DISTRIBUTION = "distribution";

    private static final String FOLDER_DISTS = "dists";
    
    private static final int DOWNLOAD_TIMEOUT_READ = 30000;
    private static final int DOWNLOAD_TIMEOUT_CONNECT = 15000;
    private static final int DOWNLOAD_BUFFER_SIZE = 4096;

    private static final String ENV_CEYLON_BOOTSTRAP_DISTS = "CEYLON_BOOTSTRAP_DISTS";
    private static final String ENV_CEYLON_BOOTSTRAP_PROPS = "CEYLON_BOOTSTRAP_PROPERTIES";
    
    private static final String PROP_CEYLON_BOOTSTRAP_DISTS = "ceylon.bootstrap.dists";
    private static final String PROP_CEYLON_BOOTSTRAP_PROPS = "ceylon.bootstrap.properties";

    private static final String VERSION_BOOTSTRAP_NAME = "CeylonBootstrap";
    private static final String VERSION_BOOTSTRAP_NUMBER = Versions.CEYLON_VERSION;

    public static void main(String[] args) throws Throwable {
        // we don't need to clean up the class loader when run from main because the JVM will either exit, or
        // keep running with daemon threads in which case it will keep needing this classloader open 
        int exit = run(args);
        // WARNING: NEVER CALL EXIT IF WE STILL HAVE DAEMON THREADS RUNNING AND WE'VE NO REASON TO EXIT WITH A NON-ZERO CODE
        if (exit != 0) {
            System.exit(exit);
        }
    }

    public static int run(String... args) throws Throwable {
        return new Bootstrap().runInternal(args);
    }
    
    public int runInternal(String... args) throws Throwable {
        boolean canRetry = false;
        String ceylonVersion;
        if (isDistBootstrap()) {
            // Load configuration
            Config cfg = loadBootstrapConfig();
            setupDistHome(cfg);
            ceylonVersion = determineDistVersion();
        } else if (distArgument(args) != null) {
            String dist = distArgument(args);
            args = stripDistArgument(args);
            Config cfg = createDistributionConfig(dist);
            setupDistHome(cfg);
            ceylonVersion = determineDistVersion();
        } else {
            ceylonVersion = LauncherUtil.determineSystemVersion();
            //canRetry = true; // Disabled for now, enable if we want automatic fall-back to the current version
        }
        try {
            if (!canRetry || Versions.CEYLON_VERSION_NUMBER.equals(ceylonVersion)) {
                // Using current Ceylon version, or no retries allowed
                return runVersion(ceylonVersion, args);
            } else {
                // Using Ceylon version different from current, if the first
                // run fails we'll retry with the current one
                try {
                    return runVersion(ceylonVersion, args);
                } catch (ClassNotFoundException ex) {
                    System.err.println("Fatal: Ceylon distribution could not be found for version: " + ceylonVersion + ", using default");
                    ceylonVersion = Versions.CEYLON_VERSION_NUMBER;
                    System.setProperty(Constants.PROP_CEYLON_SYSTEM_VERSION, ceylonVersion);
                    return runVersion(Versions.CEYLON_VERSION_NUMBER, args);
                }
            }
        } catch (ClassNotFoundException ex) {
            System.err.println("Fatal: Ceylon distribution could not be found for version: " + ceylonVersion);
            return -1;
        } catch (Exception e) {
            System.err.println("Fatal: Ceylon command could not be executed");
            if (e.getCause() != null) {
                throw e;
            } else {
                if (!(e instanceof RuntimeException) || e.getMessage() == null) {
                    System.err.println("   --> " + e.toString());
                } else {
                    System.err.println("   --> " + e.getMessage());
                }
                return -1;
            }
        }
    }
    
    private int runVersion(String ceylonVersion, String... args) throws Throwable {
        CeylonClassLoader cl = null;
        try {
            Integer result = -1;
            Method runMethod = null;
            File module = CeylonClassLoader.getRepoJar("ceylon.bootstrap", ceylonVersion);
            if (!module.exists()) {
                File homeLib = new File(System.getProperty(Constants.PROP_CEYLON_HOME_DIR), "lib");
                module = new File(homeLib, FILE_BOOTSTRAP_JAR);
            }
            cl = CeylonClassLoader.newInstance(Arrays.asList(module));
            Class<?> launcherClass = cl.loadClass("org.eclipse.ceylon.launcher.Launcher");
            runMethod = launcherClass.getMethod("run", String[].class);
            try {
                result = (Integer)runMethod.invoke(null, (Object)args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
            return result.intValue();
        } finally {
            if (cl != null) {
                try {
                    cl.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }
    
    protected boolean isDistBootstrap() throws URISyntaxException {
        File propsFile = getPropertiesFile();
        return propsFile.exists();
    }
    
    private static String distArgument(String[] args) {
        for (String arg : args) {
            if (!arg.startsWith("-")) {
                break;
            }
            if (arg.startsWith("--distribution=") && arg.length() > 15) {
                return arg.substring(15);
            }
        }
        return null;
    }

    private static String[] stripDistArgument(String[] args) {
        ArrayList<String> lst = new ArrayList<String>();
        for (String arg : args) {
            if (!arg.startsWith("--distribution=") || arg.length() <= 15) {
                lst.add(arg);
            }
        }
        String[] buf = new String[lst.size()];
        return lst.toArray(buf);
    }

    protected void setupDistHome(Config cfg) throws Exception {
        // If hash doesn't exist in dists folder we must download & install
        if (!cfg.distributionDir.exists()) {
            install(cfg);
            if (!cfg.distributionDir.exists()) {
                throw new RuntimeException("Unable to install distribution");
            }
        }
        // Set the correct home folder
        System.setProperty(Constants.PROP_CEYLON_HOME_DIR, cfg.distributionDir.getAbsolutePath());
    }
    
    private void install(Config cfg) throws Exception {
        File tmpFile = null;
        File tmpFolder = null;
        try {
            // Check if the distribution URI refers to a remote or a local file
            File zipFile;
            if (cfg.distribution.getScheme() != null) {
                // Set up a download progress monitor if we have a console
                ProgressMonitor monitor = null;
                if (System.console() != null) {
                    monitor = new ProgressMonitor() {
                        @Override
                        public void update(long read, long size) {
                            String progress;
                            if (size == -1) {
                                progress = String.valueOf(read / 1024L) + "K";
                            } else {
                                progress = String.valueOf(read * 100 / size) + "%";
                            }
                            System.out.print("Downloading Ceylon... " + progress + "\r");
                        }
                    };
                }
                // Start download of URL to temp file
                tmpFile = zipFile = File.createTempFile("ceylon-bootstrap-dist-", ".part");
                setupProxyAuthentication();
                download(cfg.distribution, zipFile, monitor);
            } else {
                // It's a local file, no need to download
                zipFile = new File(cfg.properties.getParentFile(), cfg.distribution.getPath()).getAbsoluteFile();
            }
            // Verify zip file if we have a sha sum
            if (cfg.sha256sum != null) {
                String sum = calculateSha256Sum(zipFile);
                if (!sum.equals(cfg.sha256sum)) {
                    throw new RuntimeException("Error verifying Ceylon distribution archive: SHA sums do not match");
                }
            }
            // Unzip file to temp folder in dists folder
            mkdirs(cfg.resolvedInstallation);
            tmpFolder = Files.createTempDirectory(cfg.resolvedInstallation.toPath(), "ceylon-bootstrap-dist-").toFile();
            extractArchive(zipFile, tmpFolder);
            validateDistribution(cfg, tmpFolder);
            writeDistributionInfo(cfg, tmpFolder);
            // Rename temp folder to hash
            tmpFolder.renameTo(cfg.distributionDir);
            if (System.console() != null) {
                // Clearing the download progress text on the console
                System.out.print("                              \r");
            }
        } finally {
            // Delete temp file and folder
            if (tmpFile != null) {
                delete(tmpFile);
            }
            if (tmpFolder != null) {
                delete(tmpFolder);
            }
        }
    }
    
    private static void validateDistribution(Config cfg, File tmpFolder) {
        File binDir = new File(tmpFolder, Constants.CEYLON_BIN_DIR);
        File libDir = new File(tmpFolder, "lib");
        File repoDir = new File(tmpFolder, "repo");
        boolean valid = binDir.exists() && libDir.exists() && repoDir.exists();
        if (!valid) {
            throw new RuntimeException("Not a valid Ceylon distribution archive: " + cfg.distribution);
        }
        File bootstrapLibJar = new File(libDir, FILE_BOOTSTRAP_JAR);
        if (!bootstrapLibJar.exists()) {
            throw new RuntimeException("Ceylon distribution archive is too old and not supported: " + cfg.distribution);
        }
    }

    private void writeDistributionInfo(Config cfg, File tmpFolder) throws IOException {
        writeFile(new File(tmpFolder, FILE_BS_ORIGIN), cfg.distribution.toString() + "\n");
    }
    
    private void writeFile(File file, String contents) throws IOException {
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(file);
            output.write(contents.getBytes());
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }

    private static File getPropertiesFile() throws URISyntaxException {
        String cbp;
        if ((cbp  = System.getProperty(PROP_CEYLON_BOOTSTRAP_PROPS)) != null) {
            return new File(cbp);
        } else if ((cbp  = System.getenv(ENV_CEYLON_BOOTSTRAP_PROPS)) != null) {
            return new File(cbp);
        } else {
            File jar = LauncherUtil.determineRuntimeJar();
            return new File(jar.getParentFile(), FILE_BOOTSTRAP_PROPERTIES);
        }
    }
    
    private static Properties loadBootstrapProperties() throws Exception {
        File propsFile = getPropertiesFile();
        FileInputStream fileInput = null;
        try {
            fileInput = new FileInputStream(propsFile);
            Properties properties = new Properties();
            properties.load(fileInput);
            return properties;
        } finally {
            if (fileInput != null) {
                fileInput.close();
            }
        }
    }
    
    protected static class Config {
        public Config () {}
        public File properties;
        public URI distribution;
        public File installation;
        public File resolvedInstallation;
        public File distributionDir;
        public String hash;
        public String sha256sum;
    }
    
    protected Config loadBootstrapConfig() throws Exception {
        Properties properties = loadBootstrapProperties();
        Config cfg = new Config();
        
        cfg.properties = getPropertiesFile();
        
        // Obtain dist download URL
        if (!properties.containsKey(KEY_DISTRIBUTION)) {
            throw new RuntimeException("Error in bootstrap properties file: missing 'distribution'");
        }
        cfg.distribution = new URI(properties.getProperty(KEY_DISTRIBUTION));

        // See if the distribution should be installed in some other place than the default
        if (properties.containsKey(KEY_INSTALLATION)) {
            // Get the installation path
            String installString = properties.getProperty(KEY_INSTALLATION);
            // Do some simple variable expansion
            installString = installString
                    .replaceAll("^~", System.getProperty("user.home"))
                    .replace("${user.home}", System.getProperty("user.home"))
                    .replace("${ceylon.user.dir}", getUserDir().getAbsolutePath());
            cfg.installation = new File(installString);
            cfg.resolvedInstallation = cfg.properties.getParentFile().toPath().resolve(cfg.installation.toPath()).toFile().getAbsoluteFile();
        } else {
            File distsDir;
            String distsDirStr;
            if ((distsDirStr = System.getProperty(PROP_CEYLON_BOOTSTRAP_DISTS)) != null) {
                distsDir = new File(distsDirStr);
            } else if ((distsDirStr = System.getenv(ENV_CEYLON_BOOTSTRAP_DISTS)) != null) {
                distsDir = new File(distsDirStr);
            } else {
                distsDir = new File(getUserDir(), FOLDER_DISTS);
            }
            cfg.resolvedInstallation = distsDir;
        }

        // If the properties contain a sha256sum store it for later
        cfg.sha256sum = properties.getProperty(KEY_SHA256SUM);

        return updateConfig(cfg);
    }
    
    protected Config createDistributionConfig(String dist) throws URISyntaxException {
        Config cfg = new Config();
        cfg.distribution = getDistributionUri(dist);
        return updateConfig(cfg);
    }

    protected URI getDistributionUri(String dist) throws URISyntaxException {
        URI uri = new URI(dist);
        if (uri.getScheme() != null) {
            return uri;
        } else {
            return new URI(CEYLON_DOWNLOAD_BASE_URL + dist.replace('.', '_'));
        }
    }
    
    private static Config updateConfig(Config cfg) {
        // Hash the URI, it will be our distribution's folder name
        cfg.hash = hash(cfg.distribution.toString());
        
        // Make sure resolvedInstallation points to a proper installation folder
        if (cfg.installation != null) {
            cfg.resolvedInstallation = cfg.properties.getParentFile().toPath().resolve(cfg.installation.toPath()).toFile().getAbsoluteFile();
        } else {
            cfg.resolvedInstallation = new File(getUserDir(), FOLDER_DISTS);
        }
        
        // The actual installation directory for the distribution
        cfg.distributionDir = new File(cfg.resolvedInstallation, cfg.hash);
        
        return cfg;
    }
    
    private static File mkdirs(File dir) {
        if (!dir.exists() && !dir.mkdirs()) {
            throw new RuntimeException("Unable to create destination directory: " + dir);
        }
        return dir;
    }
    
    private static void delete(File f) {
        if (!delete_(f)) {
            // As a last resort
            f.deleteOnExit();
        }
    }
    
    private static boolean delete_(File f) {
        boolean ok = true;
        if (f.exists()) {
            if (f.isDirectory()) {
                for (File c : f.listFiles()) {
                    ok = ok && delete_(c);
                }
            }
            try {
                boolean deleted = f.delete();
                ok = ok && deleted;
            } catch (Exception ex) {
                ok = false;
            }
        }
        return ok;
    }
    
    private static File getDefaultUserDir() {
        String userHome = System.getProperty("user.home");
        return new File(userHome, ".ceylon");
    }

    private static File getUserDir() {
        String ceylonUserDir = System.getProperty(Constants.PROP_CEYLON_USER_DIR);
        if (ceylonUserDir != null) {
            return new File(ceylonUserDir);
        } else {
            return getDefaultUserDir();
        }
    }
    
    private static void extractArchive(File zip, File dir) throws IOException {
        if (dir.exists()) {
            if (!dir.isDirectory()) {
                throw new RuntimeException("Error extracting archive: destination not a directory: " + dir);
            }
        } else {
            mkdirs(dir);
        }

        ZipFile zf = null;
        try {
            zf = new ZipFile(zip);
            Enumeration<? extends ZipEntry> entries = zf.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String entryName = stripRoot(entry.getName());
                try {
                    if (entryName.isEmpty()) {
                        continue;
                    }
                    File out = new File(dir, entryName);
                    if (entry.isDirectory()) {
                        mkdirs(out);
                        continue;
                    }
                    mkdirs(out.getParentFile());
                    InputStream zipIn = null;
                    try {
                        zipIn = zf.getInputStream(entry);
                        BufferedOutputStream fileOut = null;
                        try {
                            fileOut = new BufferedOutputStream(new FileOutputStream(out));
                            copyStream(zipIn, fileOut, false, false);
                        } finally {
                            if (fileOut != null) {
                                fileOut.close();
                            }
                        }
                    } finally {
                        if (zipIn != null) {
                            zipIn.close();
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Error extracting archive", e);
                }
            }
        } finally {
            if (zf != null) {
                zf.close();
            }
        }
    }
    
    private static String stripRoot(String name) {
        int p = name.indexOf('/');
        if (p > 0) {
            name = name.substring(p + 1);
        }
        return name;
    }

    private static void copyStream(InputStream in, OutputStream out, boolean closeIn, boolean closeOut) throws IOException {
        try {
            copyStreamNoClose(in, out);
        } finally {
            if (closeIn) {
                safeClose(in);
            }
            if (closeOut) {
                safeClose(out);
            }
        }
    }

    private static void copyStreamNoClose(InputStream in, OutputStream out) throws IOException {
        final byte[] bytes = new byte[8192];
        int cnt;
        while ((cnt = in.read(bytes)) != -1) {
            out.write(bytes, 0, cnt);
        }
        out.flush();
    }
    
    private static void safeClose(Closeable c) {
        try {
            if (c != null) {
                c.close();
            }
        } catch (IOException ignored) {
        }
    }
    
    /**
     * This method computes a hash of the provided {@code string}.
     * Copied from Gradle's PathAssembler
     */
    private static String hash(String string) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] bytes = string.getBytes();
            messageDigest.update(bytes);
            return new BigInteger(1, messageDigest.digest()).toString(36);
        } catch (Exception e) {
            throw new RuntimeException("Error creating hash", e);
        }
    }
    
    /**
     * This method calculates the SHA256 sum of the provided {@code file}
     * Copied from Gradle's Install
     */
    private static String calculateSha256Sum(File file) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        InputStream fis = null;
        try {
            fis = new FileInputStream(file);
            int n = 0;
            byte[] buffer = new byte[4096];
            while (n != -1) {
                n = fis.read(buffer);
                if (n > 0) {
                    md.update(buffer, 0, n);
                }
            }
            byte byteData[] = md.digest();
    
            StringBuffer hexString = new StringBuffer();
            for (int i=0; i < byteData.length; i++) {
                String hex=Integer.toHexString(0xff & byteData[i]);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
    
            return hexString.toString();
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
    }
    
    private static interface ProgressMonitor {
        void update(long read, long size);
    }
    
    protected int getReadTimeout() {
        return DOWNLOAD_TIMEOUT_READ;
    }

    protected int getConnectTimeout() {
        return DOWNLOAD_TIMEOUT_CONNECT;
    }
    /**
     * A {@link SizedInputStream} that can reconnect some number f times
     */
    class RetryingSizedInputStream {
        
        private final URL url;
        /** 
         * Whether range requests should be made when 
         * the {@link ReconnectingInputStream} has to reconnect.
         */
        private boolean rangeRequests;
        /** The number of attempts to download the resource */
        /** The total number of attempts (including the initial one) */
        private final int attempts = 3;
        private int reattemptsLeft = attempts-1;
        /**
         * For selected exceptions returns normally if there are 
         * attempts left, otherwise rethrows the given exception. 
         */
        private void giveup(URL url, IOException e) throws IOException{
            if (e instanceof SocketTimeoutException
                    || e instanceof SocketException
                    || e instanceof EOFException) {
                if (reattemptsLeft-- > 0) {
                    //log.debug("Retry download of "+ url + " after " + e + " (" + getReattemptsLeft() + " reattempts left)");
                    return;
                }
            }
            if (e instanceof SocketTimeoutException) {
                // Include url in exception message
                SocketTimeoutException newException = new SocketTimeoutException("Timed out downloading "+url);
                newException.initCause(e);
                e = newException;
            }
            //log.debug("Giving up request to " + url + " (after "+ getAttemptsMade() + " attempts) due to: " + e );
            throw e;
            
        }
        /** The <em>current</em> stream: Gets mutated when {@link ReconnectingInputStream} reconnects */
        
        private HttpURLConnection connection = null;
        private InputStream stream = null;
        long bytesRead = 0;
        private final ReconnectingInputStream reconnectingStream;
        private final long contentLength;
        
        public RetryingSizedInputStream(URL url) throws IOException {
            this.url = url;
            long length = 0;
            connecting: while (true) {
                try{
                    connection = makeConnection(url, -1);
                    int code = connection.getResponseCode();
                    if (code != -1 && code != 200) {
                        //log.info("Got " + code + " for url: " + url);
                        RuntimeException notGettable = new RuntimeException("Connection error: " + code);
                        cleanUpStreams(notGettable);
                        throw notGettable;
                    }
                    String acceptRange = connection.getHeaderField("Accept-Range");
                    rangeRequests = acceptRange == null || !acceptRange.equalsIgnoreCase("none");
                    //debug("Connection: "+connection.getHeaderField("Connection"));
                    //debug("Got " + code + " for url: " + url);
                    length = connection.getContentLengthLong();
                    stream = connection.getInputStream();
                    break connecting;
                } catch(IOException connectException) {
                    maybeRetry(url, connectException);
                }
            }
            this.contentLength = length;
            this.reconnectingStream = new ReconnectingInputStream();
        }

        private void maybeRetry(URL url, IOException e) throws IOException {
            cleanUpStreams(e);
            giveup(url, e);
        }

        /**
         * According to https://docs.oracle.com/javase/8/docs/technotes/guides/net/http-keepalive.html
         * we should read the error stream so the connection can be reused.
         */
        private void cleanUpStreams(Exception inflight) {
            if (stream != null) {
                try {
                    stream.close();
                    stream = null;
                } catch (IOException closeException) {
                    inflight.addSuppressed(closeException);
                }
            }
            
            if (connection != null) {
                byte[] buf = new byte[8*2014];
                InputStream es = connection.getErrorStream();
                if (es != null) {
                    try {
                        try {
                            while (es.read(buf) > 0) {}
                        } finally {
                            es.close();
                        }
                    } catch (IOException errorStreamError) {
                        inflight.addSuppressed(errorStreamError);
                    }
                }
            }
        }
        
        private HttpURLConnection makeConnection(URL url, long start)
                throws IOException, SocketTimeoutException {
            URLConnection conn;
            conn = url.openConnection();
            if (!(conn instanceof HttpURLConnection)) {
                throw new RuntimeException();
            }
            HttpURLConnection huc = (HttpURLConnection)conn;
            huc.setRequestProperty("User-Agent", getUserAgent());
            huc.setConnectTimeout(getConnectTimeout());
            huc.setReadTimeout(getReadTimeout());
            boolean useRangeRequest = start > 0;
            if (useRangeRequest) {
                String range = "bytes "+start+"-";
                //debug("Using Range request for" + range + " of " + url);
                huc.setRequestProperty("Range", range);
            }
            //debug("Connecting to " + url);
            conn.connect();
            return huc;
        }
        
        public long getSize() {
            return contentLength;
        }
        
        public InputStream getInputStream() {
            return reconnectingStream;
        }
        
        /**
         * An InputStream that can reconnects on SocketTimeoutException.
         * If it reconnects it makes a {@code Range} request to get just the 
         * remainder of the resource, unless {@link #rangeRequests} is false.
         */
        class ReconnectingInputStream extends InputStream {
            public void close() throws IOException {
                if (stream != null) {
                    stream.close();
                }
            }
            
            public int read(byte[] buf, int offset, int length) throws IOException {
                /*
                 * Overridden because {@link InputStream#read(byte[], int, int)}
                 * behaves badly wrt non-initial {@link #read()}s throwing.
                 */
                while (true) {
                    try {
                        int result = stream.read(buf, offset, length);
                        if (result != -1) {
                            bytesRead+=result;
                        } else {
                            // did we get all the stream?
                            if (bytesRead == getSize()) {
                                return result;
                            } else {
                                throw new EOFException();
                            }
                        }
                        return result;
                    } catch (IOException readException) {
                        recover(readException);
                    }
                }
            }
            
            @Override
            public int read() throws IOException {
                while (true) {
                    try {
                        int result = stream.read();
                        if (result != -1) {
                            bytesRead++;
                        }
                        return result;
                    } catch (IOException readException) {
                        recover(readException);
                    }
                }
            }
            
            /**
             * Reconnects, reassigning {@link RetryingSizedInputStream#connection} 
             * and {@link RetryingSizedInputStream#stream}, or 
             * throws {@code IOException} if we can't retry.
             */
            protected void recover(IOException readException) throws IOException {
                maybeRetry(url, readException);
                // if we maybeRetry didn't propage the exception let's retry...
                reconnect: while (true) {
                    try {
                        // otherwise open another connection...
                        // using a range request unless initial request had Accept-Ranges: none
                        connection = makeConnection(url, rangeRequests ? bytesRead : -1);
                        final int code = connection.getResponseCode();
                        //debug("Got " + code + " for reconnection to url: " + url);
                        if (rangeRequests && code == 206) {
                            stream = connection.getInputStream();
                        } else if (code == 200) {
                            if (rangeRequests) {
                                //debug("Looks like " + url.getHost() + ":" + url.getPort() + " does support range request, to reading first " + bytesRead + " bytes");
                            }
                            // we didn't make a range request
                            // (or the server didn't understand the Range header)
                            // so spool the appropriate number of bytes
                            stream = connection.getInputStream();
                            try {
                                for (long ii = 0; ii < bytesRead; ii++) {
                                    stream.read();
                                }
                            } catch (IOException spoolException) {
                                maybeRetry(url, spoolException);
                                continue reconnect;
                            }
                        } else {
                            throw new RuntimeException("Connection error: " + code + " on reconnect");
                        }
                        //debug("Reconnected to url: " + url);
                        break reconnect;
                    } catch (IOException reconnectionException) {
                        maybeRetry(url, reconnectionException);
                    }
                }
            }
        }
    }
    
    private void download(URI uri, File file, ProgressMonitor progress) throws IOException {
        InputStream input = null;
        OutputStream output = null;
        try {
            URL url = uri.toURL();
            RetryingSizedInputStream r = new RetryingSizedInputStream(url);
            input = r.getInputStream();
            output = new FileOutputStream(file);
            int n;
            long read = 0;
            long size = r.getSize();
            byte[] buffer = new byte[DOWNLOAD_BUFFER_SIZE];
            while ((n = input.read(buffer)) != -1) {
                output.write(buffer, 0, n);
                read += n;
                if (progress != null) {
                    progress.update(read, size);
                }
            }
        } finally {
            if (output != null) {
                output.close();
            }
            if (input != null) {
                input.close();
            }
        }
    }
    
    /**
     * Sets up proxy authentication if the associated system properties
     * are available: "http.proxyUser" and "http.proxyPassword"
     * Copied from Gradle's Download
     */
    private static void setupProxyAuthentication() {
        if (System.getProperty("http.proxyUser") != null) {
            Authenticator.setDefault(new ProxyAuthenticator());
        }
    }
    
    private static class ProxyAuthenticator extends Authenticator {
        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(
                    System.getProperty("http.proxyUser"),
                    System.getProperty("http.proxyPassword", "").toCharArray());
        }
    }

    /**
     * Sets up a User Agent string to be able to unique identify this tool in all the web traffic
     * Copied from Gradle's Download
     */
    private String getUserAgent() {
        String javaVendor = System.getProperty("java.vendor");
        String javaVersion = System.getProperty("java.version");
        String javaVendorVersion = System.getProperty("java.vm.version");
        String osName = System.getProperty("os.name");
        String osVersion = System.getProperty("os.version");
        String osArch = System.getProperty("os.arch");
        return String.format("%s/%s (%s;%s;%s) (%s;%s;%s)", VERSION_BOOTSTRAP_NAME, VERSION_BOOTSTRAP_NUMBER, osName, osVersion, osArch, javaVendor, javaVersion, javaVendorVersion);
    }
    
    private static File determineDistLanguage(File distHome) {
        File distRepo = new File(distHome, "repo");
        File bootstrap = new File(new File(distRepo, "ceylon"), "language");
        File[] versions = bootstrap.listFiles(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory();
            }
        });
        if (versions == null || versions.length != 1) {
            return null;
        }
        return versions[0];
    }

    private static String determineDistVersion() {
        File distHome = new File(System.getProperty(Constants.PROP_CEYLON_HOME_DIR));
        File versionDir = determineDistLanguage(distHome);
        if (versionDir == null) {
            throw new RuntimeException("Error in distribution: missing bootstrap in " + distHome.getAbsolutePath());
        }
        return versionDir.getName();
    }
}
