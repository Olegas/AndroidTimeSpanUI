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
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class TimeSpanGroupEditActivity extends Activity {

    final static String GROUP_SPEC_EXTRA = "ru.elifantiev.android.timespan.GROUP_SPEC_EXTRA";
    private TimeSpanGroupEditor groupEditor;

    @Override
    public void onBackPressed() {
        if(groupEditor.getValue().getDayMask() == 0) {
            Toast.makeText(this, R.string.selectDays, Toast.LENGTH_LONG).show();
        } else {
            Intent result = new Intent();
            result.putExtra(GROUP_SPEC_EXTRA, groupEditor.getValue().toString());
            setResult(RESULT_OK, result);
            finish();
        }
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
        }
    }
}