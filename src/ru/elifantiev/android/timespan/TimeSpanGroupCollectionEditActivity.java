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

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

public class TimeSpanGroupCollectionEditActivity extends ListActivity {

    static final String GROUP_SPEC_EXTRA = "ru.elifantiev.android.timespan.GROUP_SPEC_EXTRA";

    private List<TimeSpanGroup> groupCollection;

    private Button btnAddNew;

    @Override
    public void onBackPressed() {
        Intent result = new Intent();
        result.putExtra(
                GROUP_SPEC_EXTRA,
                TimeSpanGroupCollection.toString(new TreeSet<TimeSpanGroup>(groupCollection)));
        setResult(RESULT_OK, result);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK && data != null) {
            String result = data.getStringExtra(TimeSpanGroupEditActivity.GROUP_SPEC_EXTRA);
            if(result != null && !"".equals(result)) {
                TimeSpanGroup gResult = TimeSpanGroup.valueOf(result);
                if(requestCode > 0) {
                    groupCollection.set(requestCode - 1, gResult);
                    ((ArrayAdapter)getListView().getAdapter()).notifyDataSetInvalidated();
                } else if(requestCode == 0) {
                    groupCollection.add(gResult);
                    ((ArrayAdapter)getListView().getAdapter()).notifyDataSetChanged();
                }
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.mnuDelete) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

            ListView list = getListView();
            ArrayAdapter adapter = (ArrayAdapter)(list.getAdapter());
            groupCollection.remove(info.position);
            adapter.notifyDataSetChanged();
            return true;
        }
        return false;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        if (view instanceof ListView) {
            getMenuInflater().inflate(R.menu.span_list_menu, menu);
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.time_span_group_collection_editor);

        btnAddNew = (Button)findViewById(R.id.btnAddGroup);

        String spec = getIntent().getStringExtra(GROUP_SPEC_EXTRA);
        if(spec != null && !"".equals(spec))
            groupCollection = new ArrayList<TimeSpanGroup>(TimeSpanGroupCollection.valueOf(spec));
        else {
            finish();
            return;
        }

        ListView list = getListView();

        list.setAdapter(new TimeSpanGroupAdapter(
                this,
                R.layout.time_span_group_list_item,
                groupCollection
        ));

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent call = new Intent(TimeSpanGroupCollectionEditActivity.this, TimeSpanGroupEditActivity.class);
                call.putExtra(TimeSpanGroupEditActivity.GROUP_SPEC_EXTRA, groupCollection.get(i).toString());
                startActivityForResult(call, i + 1);
            }
        });

        registerForContextMenu(list);

        btnAddNew.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent call = new Intent(TimeSpanGroupCollectionEditActivity.this, TimeSpanGroupEditActivity.class);
                startActivityForResult(call, 0);
            }
        });
    }

    class TimeSpanGroupAdapter extends ArrayAdapter<TimeSpanGroup> {

        public TimeSpanGroupAdapter(Context context, int textViewResourceId, List<TimeSpanGroup> objects) {
            super(context, textViewResourceId, objects);
        }

        TimeSpanGroupAdapter(Context context, int resource, int textViewResourceId, List<TimeSpanGroup> objects) {
            super(context, resource, textViewResourceId, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = super.getView(position, convertView, parent);
            ((TextView)v).setText(this.getItem(position).toReadableString(getContext()));
            return v;
        }
    }

}