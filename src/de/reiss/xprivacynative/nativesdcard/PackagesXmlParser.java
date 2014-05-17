package de.reiss.xprivacynative.nativesdcard;

import android.content.Context;
import android.util.Log;
import android.util.Xml;
import de.reiss.xprivacynative.Global;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.*;
import java.util.ArrayList;

public class PackagesXmlParser {


    // We don't use namespaces
    private static final String ns = null;

    private static final String PACKAGES = "packages";

    private static final String PACKAGE = "package";
    private static final String PACKAGE_ATTR_NAME = "name";
    private static final String PACKAGE_ATTR_USERID = "userId";
    private static final String PACKAGE_ATTR_SHAREDUSERID = "sharedUserId";


    public static ArrayList<InstalledApp> getAllInstalledApps(FileInputStream fis) {
        ArrayList<InstalledApp> apps = null;
        try {
            apps = parse(fis);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return apps;
    }

    public static ArrayList<InstalledApp> parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readAllInstalledApps(parser);
        } catch (Exception e) {

            return null;
        } finally {
            in.close();
        }
    }


    private static ArrayList<InstalledApp> readAllInstalledApps(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        ArrayList<InstalledApp> allInstalledApps = new ArrayList<InstalledApp>();

        parser.require(XmlPullParser.START_TAG, ns, PACKAGES);
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String tag = parser.getName();
            if (tag.equals(PACKAGE)) {
                InstalledApp installedApp = readPackage(parser);
                if (installedApp != null) {
                    allInstalledApps.add(installedApp);
                }
            } else {
                skip(parser);
            }
        }
        return allInstalledApps;
    }


    private static InstalledApp readPackage(XmlPullParser parser) {
        InstalledApp result = new InstalledApp();
        try {
            parser.require(XmlPullParser.START_TAG, ns, PACKAGE);

            result.packageName = parser.getAttributeValue(null, PACKAGE_ATTR_NAME);

            result.userId = parser.getAttributeValue(null, PACKAGE_ATTR_USERID);
            if (result.userId == null) {
                result.userId = parser.getAttributeValue(null, PACKAGE_ATTR_SHAREDUSERID);
            }

            // skip the rest
            while (parser.next() != XmlPullParser.END_TAG) {
                // only call skip if start tag, otherwise just get the next tag
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                skip(parser);
            }
            parser.require(XmlPullParser.END_TAG, ns, PACKAGE);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    private static void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    private String readFromFileUsingContext(Context context, String fileName) {
        StringBuffer sb = new StringBuffer();

        final String success = String.format("Reading file %s worked!", fileName);
        final String fail = String.format("Reading file %s did not work!", fileName);

        StringBuffer buffer = new StringBuffer();

        try {
            FileInputStream fis = context.openFileInput(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            String read;
            if (fis != null) {
                while ((read = reader.readLine()) != null
                        && buffer.length() <= 1024) {
                    buffer.append(read + "\n");
                }
            }
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(Global.TAG, "error: " + e.getMessage());
            sb.append(fail);
            sb.append("\n");
            sb.append(e.toString());
            return sb.toString();
        }

        sb.append(success);
        sb.append("\nContent of file:\n" + buffer.toString());
        return sb.toString();
    }
}