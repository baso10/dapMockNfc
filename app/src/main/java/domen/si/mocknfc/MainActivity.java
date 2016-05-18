package domen.si.mocknfc;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    final int TECH_NFC_A = 1;
    final String EXTRA_NFC_A_SAK = "sak";    // short (SAK byte value)
    final String EXTRA_NFC_A_ATQA = "atqa";  // byte[2] (ATQA value)

    final int TECH_NFC_B = 2;
    final String EXTRA_NFC_B_APPDATA = "appdata";    // byte[] (Application Data bytes from ATQB/SENSB_RES)
    final String EXTRA_NFC_B_PROTINFO = "protinfo";  // byte[] (Protocol Info bytes from ATQB/SENSB_RES)

    final int TECH_ISO_DEP = 3;
    final String EXTRA_ISO_DEP_HI_LAYER_RESP = "hiresp";  // byte[] (null for NfcA)
    final String EXTRA_ISO_DEP_HIST_BYTES = "histbytes";  // byte[] (null for NfcB)

    final int TECH_NFC_F = 4;
    final String EXTRA_NFC_F_SC = "systemcode";  // byte[] (system code)
    final String EXTRA_NFC_F_PMM = "pmm";        // byte[] (manufacturer bytes)

    final int TECH_NFC_V = 5;
    final String EXTRA_NFC_V_RESP_FLAGS = "respflags";  // byte (Response Flag)
    final String EXTRA_NFC_V_DSFID = "dsfid";           // byte (DSF ID)

    final int TECH_NDEF = 6;
    final String EXTRA_NDEF_MSG = "ndefmsg";              // NdefMessage (Parcelable)
    final String EXTRA_NDEF_MAXLENGTH = "ndefmaxlength";  // int (result for getMaxSize())
    final String EXTRA_NDEF_CARDSTATE = "ndefcardstate";  // int (1: read-only, 2: read/write, 3: unknown)
    final String EXTRA_NDEF_TYPE = "ndeftype";            // int (1: T1T, 2: T2T, 3: T3T, 4: T4T, 101: MF Classic, 102: ICODE)

    final int TECH_NDEF_FORMATABLE = 7;

    final int TECH_MIFARE_CLASSIC = 8;

    final int TECH_MIFARE_ULTRALIGHT = 9;
    final String EXTRA_MIFARE_ULTRALIGHT_IS_UL_C = "isulc";  // boolean (true: Ultralight C)

    final int TECH_NFC_BARCODE = 10;
    final String EXTRA_NFC_BARCODE_BARCODE_TYPE = "barcodetype";  // int (1: Kovio/ThinFilm)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button clickButton = (Button) findViewById(R.id.button);
        clickButton.setOnClickListener( new View.OnClickListener() {

            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v) {
                Class tagClass = Tag.class;
                Method createMockTagMethod = null;
                try {
                    createMockTagMethod = tagClass.getMethod("createMockTag", byte[].class, int[].class, Bundle[].class);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "NoSuchMethodException.", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }


                Bundle nfcaBundle = new Bundle();
                nfcaBundle.putByteArray(EXTRA_NFC_A_ATQA, new byte[]{ (byte)0x44, (byte)0x00 }); //ATQA for Type 2 tag
                nfcaBundle.putShort(EXTRA_NFC_A_SAK , (short)0x00); //SAK for Type 2 tag

                Bundle ndefBundle = new Bundle();
                ndefBundle.putInt(EXTRA_NDEF_MAXLENGTH, 48); // maximum message length: 48 bytes
                ndefBundle.putInt(EXTRA_NDEF_CARDSTATE, 1); // read-only
                ndefBundle.putInt(EXTRA_NDEF_TYPE, 2); // Type 2 tag

                NdefMessage myNdefMessage = null; // create an NDEF message
                try {
                    myNdefMessage = new NdefMessage(NdefRecord.createMime("text/plain", ("Test Nfc").getBytes("US-ASCII")));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                ndefBundle.putParcelable(EXTRA_NDEF_MSG, myNdefMessage);  // add an NDEF message

                byte[] tagId = new byte[] { (byte)0x3F, (byte)0x12, (byte)0x34, (byte)0x56, (byte)0x78, (byte)0x90, (byte)0xAB };

                Tag mockTag = null;
                try {
                    mockTag = (Tag)createMockTagMethod.invoke(null,
                            tagId,                                     // tag UID/anti-collision identifier (see Tag.getId() method)
                            new int[] { TECH_NFC_A, TECH_NDEF },       // tech-list
                            new Bundle[] { nfcaBundle, ndefBundle });  // array of tech-extra bundles, each entry maps to an entry in the tech-list
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }

                Intent ndefIntent  = new Intent(NfcAdapter.ACTION_NDEF_DISCOVERED);
                ndefIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                ndefIntent.setType("text/plain");
                ndefIntent.putExtra(NfcAdapter.EXTRA_ID, tagId);
                ndefIntent.putExtra(NfcAdapter.EXTRA_TAG, mockTag);
                ndefIntent.putExtra(NfcAdapter.EXTRA_NDEF_MESSAGES, new NdefMessage[]{ myNdefMessage });  // optionally add an NDEF message

                boolean activityExists = ndefIntent.resolveActivityInfo(getPackageManager(), 0) != null;
                if(activityExists) {
                    startActivity(ndefIntent);
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "No activity.", Toast.LENGTH_LONG).show();
                }
            }
        });


    }
}
