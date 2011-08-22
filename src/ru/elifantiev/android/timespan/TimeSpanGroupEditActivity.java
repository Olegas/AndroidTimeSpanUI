/*
 * Copyright 2011 Oleg Elifantiev
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package ru.elifantiev.android.timespan;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class TimeSpanGroupEditActivity extends Activity {

    final static String GROUP_SPEC_EXTRA = "ru.elifantiev.android.timespan.GROUP_SPEC_EXTRA";
    final static String SHOW_HELP = "ru.elifantiev.android.timespan.SHOW_HELP";
    final static int DIALOG_HELP = 1;
    private TimeSpanGroupEditor groupEditor;
    private String initialGroup = "0:0-1440";

    @Override
    public void onBackPressed() {
        final TimeSpanGroup g = groupEditor.getValue();
        if(!initialGroup.equals(g.toString())) {
            new AlertDialog.Builder(this)
                .setMessage(getString(R.string.saveChanges))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if(g.getDayMask() == 0) {
                            Toast.makeText(
                                    TimeSpanGroupEditActivity.this,
                                    R.string.selectDays,
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Intent result = new Intent();
                            result.putExtra(GROUP_SPEC_EXTRA, g.toString());
                            setResult(RESULT_OK, result);
                            finish();
                        }
                    }
                })
                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        finish();
                    }
                })
                .create()
                .show();
        }
        else
            finish();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        if(id == DIALOG_HELP) {
            dialog = new Dialog(getApplicationContext());

            dialog.setContentView(R.layout.help_dialog);
            dialog.setTitle(getString(R.string.edit_help_title));

        }
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.time_span_group_editor);
        groupEditor = (TimeSpanGroupEditor)findViewById(R.id.sel);

        Intent call = getIntent();
        String spanSpec = call.getStringExtra(GROUP_SPEC_EXTRA);
        if(spanSpec != null && !"".equals(spanSpec)) {
            groupEditor.setValue(TimeSpanGroup.valueOf(spanSpec));
            initialGroup = groupEditor.getValue().toString();
        }
    }
}