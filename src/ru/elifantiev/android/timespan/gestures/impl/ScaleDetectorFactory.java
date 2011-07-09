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
import android.os.Build;
import android.view.ScaleGestureDetector;
import ru.elifantiev.android.timespan.gestures.ScaleGestureDetectorWrapper;
import ru.elifantiev.android.timespan.gestures.SimpliestScaleListener;

public class ScaleDetectorFactory {

    public static ScaleGestureDetectorWrapper newDetector(Context ctx, final SimpliestScaleListener listener) {
        final int sdkVersion = Build.VERSION.SDK_INT;
        if (sdkVersion <= Build.VERSION_CODES.ECLAIR_MR1) {
            return  new PreFroyoScaleGestureDetector(ctx, new PreFroyoScaleGestureDetector.OnScaleGestureListener() {
                @Override
                public boolean onScale(PreFroyoScaleGestureDetector detector) {
                    if(listener != null)
                        listener.onScale(detector.getScaleFactor());
                    return true;
                }

                @Override
                public boolean onScaleBegin(PreFroyoScaleGestureDetector detector) {
                    return true;
                }

                @Override
                public void onScaleEnd(PreFroyoScaleGestureDetector detector) {

                }
            });
        } else {
            return new FroyoScaleGestureDetector(ctx, new ScaleGestureDetector.OnScaleGestureListener() {
                @Override
                public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
                    listener.onScale(scaleGestureDetector.getScaleFactor());
                    return true;
                }

                @Override
                public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
                    return true;
                }

                @Override
                public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {

                }
            });
        }
    }

}
