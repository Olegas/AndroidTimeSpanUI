package ru.elifantiev.android.timespan;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
        setValue(Arrays.asList(defaultValue));
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
        StringBuilder result = new StringBuilder();
        for (TimeSpanGroup group : groups)
            result.append(group.toReadableString(getContext())).append("\n");
        setText(result.toString());
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
            if(spec != null && !"".equals(spec)) {
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
