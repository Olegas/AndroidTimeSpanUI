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
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import java.util.*;

public class TimeSpanSelector extends TextView {

    static final int EDIT_TIME_SPEC_REQUEST = 10000;

    private List<TimeSpanGroup> groups = new ArrayList<TimeSpanGroup>();
    private TimeSpanGroup defaultValue = TimeSpanGroup.anytimeGroup();
    private OnChangeListener listener = null;
    private ActivityRequestHandler activityRequestHandler = null;

    public TimeSpanSelector(Context context) {
        super(context);
        init();
    }

    public TimeSpanSelector(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TimeSpanSelector(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setEllipsize(TextUtils.TruncateAt.MARQUEE);
        setTextColor(R.color.black);
        setFocusable(true);
        setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.keyboard_key_feedback_more_background));
        setSingleLine(true);
        setText(getContext().getString(R.string.anytime));
        setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                Intent call = new Intent(getContext(), TimeSpanGroupCollectionEditActivity.class);
                call.putExtra(
                        TimeSpanGroupCollectionEditActivity.GROUP_SPEC_EXTRA,
                        TimeSpanGroupCollection.toString(groups));
                if(activityRequestHandler != null)
                    activityRequestHandler.requestActivityStart(call, EDIT_TIME_SPEC_REQUEST);
            }
        });
    }

    public synchronized void setValue(Collection<TimeSpanGroup> groups) {
        this.groups.clear();
        this.groups.addAll(groups);
        setValueInternal(groups);
    }

    public Collection<TimeSpanGroup> getValue() {
        return Collections.unmodifiableList(groups);
    }

    private void setValueInternal(Collection<TimeSpanGroup> groups) {
        if(groups.size() > 0) {
            StringBuilder result = new StringBuilder();
            for (TimeSpanGroup group : groups)
                result.append(group.toReadableString(getContext())).append("; ");
            setText(result.toString());
        }
        else
            setText(getContext().getString(R.string.anytime));
    }

    public void setOnChangeListener(OnChangeListener listener) {
        this.listener = listener;
    }

    public void setActivityRequestHandler(ActivityRequestHandler handler) {
        this.activityRequestHandler = handler;
    }

    public boolean handleActivityResult(Intent data, int requestCode) {
        if(requestCode == EDIT_TIME_SPEC_REQUEST && data != null) {
            String spec = data.getStringExtra(TimeSpanGroupCollectionEditActivity.GROUP_SPEC_EXTRA);
            if(spec != null) {
                Collection<TimeSpanGroup> result = TimeSpanGroupCollection.valueOf(spec);
                if(listener != null)
                    listener.onChange(this, result);
                setValue(result);
                return true;
            }
        }
        return false;
    }

    public interface ActivityRequestHandler {
        void requestActivityStart(Intent call, int requestCode);
    }

    public interface OnChangeListener {
        void onChange(TimeSpanSelector selector, Collection<TimeSpanGroup> value);
    }

}
