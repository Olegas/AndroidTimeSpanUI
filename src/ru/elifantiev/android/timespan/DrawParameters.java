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

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

final class DrawParameters {
    final static int SIDE_PAD = 7;
    final float TB_PAD;
    final static int KNOB_TOUCH_AREA = 25;
    final static int MIDDLE_AREA_PAD = 15;
    final float DAY_SELECTOR_AREA_WIDTH;
    final float SCALE_LABEL_TOP_PADDING;
    final float density;

    DrawParameters(Context context) {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wmgr = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        wmgr.getDefaultDisplay().getMetrics(metrics);

        density = metrics.density;

        TB_PAD = 20 * density;
        DAY_SELECTOR_AREA_WIDTH = 60 * density;
        SCALE_LABEL_TOP_PADDING = 14 * density;
    }
}
