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

package ru.elifantiev.android.timespan.gestures.impl;

import android.content.Context;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import ru.elifantiev.android.timespan.gestures.ScaleGestureDetectorWrapper;


class FroyoScaleGestureDetector implements ScaleGestureDetectorWrapper {

    private ScaleGestureDetector detector;

    public FroyoScaleGestureDetector(Context ctx, ScaleGestureDetector.OnScaleGestureListener listener) {
        detector = new ScaleGestureDetector(ctx, listener);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return detector.onTouchEvent(event);
    }

}
