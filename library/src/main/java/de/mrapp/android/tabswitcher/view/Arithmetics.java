/*
 * Copyright 2016 - 2017 Michael Rapp
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package de.mrapp.android.tabswitcher.view;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.FrameLayout;

import de.mrapp.android.tabswitcher.R;
import de.mrapp.android.tabswitcher.TabSwitcher;
import de.mrapp.android.tabswitcher.model.Axis;

import static de.mrapp.android.util.Condition.ensureNotNull;
import static de.mrapp.android.util.Condition.ensureTrue;

/**
 * Provides methods, which allow to calculate the position, size and rotation of a {@link
 * TabSwitcher}'s children.
 *
 * @author Michael Rapp
 */
public class Arithmetics {

    /**
     * The tab switcher, the arithmetics are calculated for.
     */
    private final TabSwitcher tabSwitcher;

    /**
     * The height of a tab's title container in pixels.
     */
    private final int tabTitleContainerHeight;

    /**
     * The inset of tabs in pixels.
     */
    private final int tabInset;

    /**
     * The number of tabs, which are contained by a stack.
     */
    private final int stackedTabCount;

    /**
     * The space between tabs, which are part of a stack, in pixels.
     */
    private final float stackedTabSpacing;

    /**
     * The maximum space between two neighboring tabs.
     */
    private final float maxTabSpacing;

    /**
     * Modifies a specific axis depending on the orientation of the tab switcher.
     *
     * @param axis
     *         The original axis as a value of the enum {@link Axis}. The axis may not be null
     * @return The orientation invariant axis as a value of the enum {@link Axis}. The orientation
     * invariant axis may not be null
     */
    @NonNull
    private Axis getOrientationInvariantAxis(@NonNull final Axis axis) {
        if (tabSwitcher.isDraggingHorizontally()) {
            return axis == Axis.DRAGGING_AXIS ? Axis.ORTHOGONAL_AXIS : Axis.DRAGGING_AXIS;
        }

        return axis;
    }

    /**
     * Creates a new class, which provides methods, which allow to calculate the position, size and
     * rotation of a {@link TabSwitcher}'s children.
     *
     * @param tabSwitcher
     *         The tab switcher, the arithmetics should be calculated for, as an instance of the
     *         class {@link TabSwitcher}. The tab switcher may not be null
     */
    public Arithmetics(@NonNull final TabSwitcher tabSwitcher) {
        ensureNotNull(tabSwitcher, "The tab switcher may not be null");
        this.tabSwitcher = tabSwitcher;
        Resources resources = tabSwitcher.getResources();
        this.tabTitleContainerHeight =
                resources.getDimensionPixelSize(R.dimen.tab_title_container_height);
        this.tabInset = resources.getDimensionPixelSize(R.dimen.tab_inset);
        this.stackedTabCount = resources.getInteger(R.integer.stacked_tab_count);
        this.stackedTabSpacing = resources.getDimensionPixelSize(R.dimen.stacked_tab_spacing);
        this.maxTabSpacing = resources.getDimensionPixelSize(R.dimen.max_tab_spacing);
    }

    /**
     * Returns the position of a motion event on a specific axis.
     *
     * @param axis
     *         The axis as a value of the enum {@link Axis}. The axis may not be null
     * @param event
     *         The motion event, whose position should be returned, as an instance of the class
     *         {@link MotionEvent}. The motion event may not be null
     * @return The position of the given motion event on the given axis as a {@link Float} value
     */
    public final float getPosition(@NonNull final Axis axis, @NonNull final MotionEvent event) {
        ensureNotNull(axis, "The axis may not be null");
        ensureNotNull(event, "The motion event may not be null");

        if (getOrientationInvariantAxis(axis) == Axis.DRAGGING_AXIS) {
            return event.getY();
        } else {
            return event.getX();
        }
    }

    /**
     * Returns the position of a view on a specific axis.
     *
     * @param axis
     *         The axis as a value of the enum {@link Axis}. The axis may not be null
     * @param view
     *         The view, whose position should be returned, as an instance of the class {@link
     *         View}. The view may not be null
     * @return The position of the given view on the given axis as a {@link Float} value
     */
    public final float getPosition(@NonNull final Axis axis, @NonNull final View view) {
        ensureNotNull(axis, "The axis may not be null");
        ensureNotNull(view, "The view may not be null");

        if (getOrientationInvariantAxis(axis) == Axis.DRAGGING_AXIS) {
            return view.getY() -
                    (tabSwitcher.isToolbarShown() && tabSwitcher.isSwitcherShown() ?
                            tabSwitcher.getToolbar().getHeight() - tabInset : 0) -
                    getPadding(axis, Gravity.START, tabSwitcher);
        } else {
            FrameLayout.LayoutParams layoutParams =
                    (FrameLayout.LayoutParams) view.getLayoutParams();
            return view.getX() - layoutParams.leftMargin - tabSwitcher.getPaddingLeft() / 2f +
                    tabSwitcher.getPaddingRight() / 2f +
                    (tabSwitcher.isDraggingHorizontally() ?
                            stackedTabCount * stackedTabSpacing / 2f : 0);
        }
    }

    /**
     * Sets the position of a view on a specific axis.
     *
     * @param axis
     *         The axis as a value of the enum {@link Axis}. The axis may not be null
     * @param view
     *         The view, whose position should be set, as an instance of the class {@link View}. The
     *         view may not be null
     * @param position
     *         The position, which should be set, as a {@link Float} value
     */
    public final void setPosition(@NonNull final Axis axis, @NonNull final View view,
                                  final float position) {
        ensureNotNull(axis, "The axis may not be null");
        ensureNotNull(view, "The view may not be null");

        if (getOrientationInvariantAxis(axis) == Axis.DRAGGING_AXIS) {
            view.setY((tabSwitcher.isToolbarShown() && tabSwitcher.isSwitcherShown() ?
                    tabSwitcher.getToolbar().getHeight() - tabInset : 0) +
                    getPadding(axis, Gravity.START, tabSwitcher) + position);
        } else {
            FrameLayout.LayoutParams layoutParams =
                    (FrameLayout.LayoutParams) view.getLayoutParams();
            view.setX(position + layoutParams.leftMargin + tabSwitcher.getPaddingLeft() / 2f -
                    tabSwitcher.getPaddingRight() / 2f -
                    (tabSwitcher.isDraggingHorizontally() ?
                            stackedTabCount * stackedTabSpacing / 2f : 0));
        }
    }

    /**
     * Animates the position of a view on a specific axis.
     *
     * @param axis
     *         The axis as a value of the enum {@link Axis}. The axis may not be null
     * @param animator
     *         The animator, which should be used to animate the position, as an instance of the
     *         class {@link ViewPropertyAnimator}. The animator may not be null
     * @param view
     *         The view, whose position should be animated, as an instance of the class {@link
     *         View}. The view may not be null
     * @param position
     *         The position, which should be set by the animation, as a {@link Float} value
     * @param includePadding
     *         True, if the view's padding should be taken into account, false otherwise
     */
    public final void animatePosition(@NonNull final Axis axis,
                                      @NonNull final ViewPropertyAnimator animator,
                                      @NonNull final View view, final float position,
                                      final boolean includePadding) {
        ensureNotNull(axis, "The axis may not be null");
        ensureNotNull(animator, "The animator may not be null");
        ensureNotNull(view, "The view may not be null");

        if (getOrientationInvariantAxis(axis) == Axis.DRAGGING_AXIS) {
            animator.y((tabSwitcher.isToolbarShown() && tabSwitcher.isSwitcherShown() ?
                    tabSwitcher.getToolbar().getHeight() - tabInset : 0) +
                    (includePadding ? getPadding(axis, Gravity.START, tabSwitcher) : 0) + position);
        } else {
            FrameLayout.LayoutParams layoutParams =
                    (FrameLayout.LayoutParams) view.getLayoutParams();
            animator.x(position + layoutParams.leftMargin +
                    (includePadding ?
                            tabSwitcher.getPaddingLeft() / 2f - tabSwitcher.getPaddingRight() / 2f :
                            0) - (tabSwitcher.isDraggingHorizontally() ?
                    stackedTabCount * stackedTabSpacing / 2f : 0));
        }
    }

    /**
     * Returns the padding of a view on a specific axis and using a specific gravity.
     *
     * @param axis
     *         The axis as a value of the enum {@link Axis}. The axis may not be null
     * @param gravity
     *         The gravity as an {@link Integer} value. The gravity must be
     *         <code>Gravity.START</code> or <code>Gravity.END</code>
     * @param view
     *         The view, whose padding should be returned, as an instance of the class {@link View}.
     *         The view may not be null
     * @return The padding of the given view on the given axis and using the given gravity as an
     * {@link Integer} value
     */
    public final int getPadding(@NonNull final Axis axis, final int gravity,
                                @NonNull final View view) {
        ensureNotNull(axis, "The axis may not be null");
        ensureTrue(gravity == Gravity.START || gravity == Gravity.END, "Invalid gravity");
        ensureNotNull(view, "The view may not be null");

        if (getOrientationInvariantAxis(axis) == Axis.DRAGGING_AXIS) {
            return gravity == Gravity.START ? view.getPaddingTop() : view.getPaddingBottom();
        } else {
            return gravity == Gravity.START ? view.getPaddingLeft() : view.getPaddingRight();
        }
    }

    /**
     * Returns the scale of a view, depending on its margin.
     *
     * @param view
     *         The view, whose scale should be returned, as an instance of the class {@link View}.
     *         The view may not be null
     * @param includePadding
     *         True, if the view's padding should be taken into account as well, false otherwise
     * @return The scale of the given view as a {@link Float} value
     */
    public final float getScale(@NonNull final View view, final boolean includePadding) {
        ensureNotNull(view, "The view may not be null");
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
        float width = view.getWidth();
        float targetWidth = width + layoutParams.leftMargin + layoutParams.rightMargin -
                (includePadding ? tabSwitcher.getPaddingLeft() + tabSwitcher.getPaddingRight() :
                        0) -
                (tabSwitcher.isDraggingHorizontally() ? stackedTabCount * stackedTabSpacing : 0);
        return targetWidth / width;
    }

    /**
     * Sets the scale of a view on a specific axis.
     *
     * @param axis
     *         The axis as a value of the enum {@link Axis}. The axis may not be null
     * @param view
     *         The view, whose scale should be set, as an instance of the class {@link View}. The
     *         view may not be null
     * @param scale
     *         The scale, which should be set, as a {@link Float} value
     */
    public final void setScale(@NonNull final Axis axis, @NonNull final View view,
                               final float scale) {
        ensureNotNull(axis, "The axis may not be null");
        ensureNotNull(view, "The view may not be null");

        if (getOrientationInvariantAxis(axis) == Axis.DRAGGING_AXIS) {
            view.setScaleY(scale);
        } else {
            view.setScaleX(scale);
        }
    }

    /**
     * Animates the scale of a view on a specific axis.
     *
     * @param axis
     *         The axis as a value of the enum {@link Axis}. The axis may not be null
     * @param animator
     *         The animator, which should be used to animate the scale, as an instance of the class
     *         {@link ViewPropertyAnimator}. The animator may not be null
     * @param scale
     *         The scale, which should be set by the animation, as a {@link Float} value
     */
    public final void animateScale(@NonNull final Axis axis,
                                   @NonNull final ViewPropertyAnimator animator,
                                   final float scale) {
        ensureNotNull(axis, "The axis may not be null");
        ensureNotNull(animator, "The animator may not be null");

        if (getOrientationInvariantAxis(axis) == Axis.DRAGGING_AXIS) {
            animator.scaleY(scale);
        } else {
            animator.scaleX(scale);
        }
    }

    /**
     * Returns the size of a view on a specific axis. By default, the view's padding is not taken
     * into account.
     *
     * @param axis
     *         The axis as a value of the enum {@link Axis}. The axis may not be null
     * @param view
     *         The view, whose size should be returned, as an instance of the class {@link View}.
     *         The view may not be null
     * @return The size of the given view on the given axis as a {@link Float} value
     */
    public final float getSize(@NonNull final Axis axis, @NonNull final View view) {
        return getSize(axis, view, false);
    }

    /**
     * Returns the size of a view on a specific axis.
     *
     * @param axis
     *         The axis as a value of the enum {@link Axis}. The axis may not be null
     * @param view
     *         The view, whose size should be returned, as an instance of the class {@link View}.
     *         The view may not be null
     * @param includePadding
     *         True, if the view's padding should be taken into account, false otherwise
     * @return The size of the given view on the given axis as a {@link Float} value
     */
    public final float getSize(@NonNull final Axis axis, @NonNull final View view,
                               final boolean includePadding) {
        ensureNotNull(axis, "The axis may not be null");
        ensureNotNull(view, "The view may not be null");

        if (getOrientationInvariantAxis(axis) == Axis.DRAGGING_AXIS) {
            return view.getHeight() * getScale(view, includePadding);
        } else {
            return view.getWidth() * getScale(view, includePadding);
        }
    }

    /**
     * Returns the default pivot of a view on a specific axis.
     *
     * @param axis
     *         The axis as a value of the enum {@link Axis}. The axis may not be null
     * @param view
     *         The view, whose pivot should be returned, as an instance of the class {@link View}.
     *         The view may not be null
     * @return The pivot of the given view on the given axis as a {@link Float} value
     */
    public final float getDefaultPivot(@NonNull final Axis axis, @NonNull final View view) {
        ensureNotNull(axis, "The axis may not be null");
        ensureNotNull(view, "The view may not be null");

        if (axis == Axis.DRAGGING_AXIS) {
            return tabSwitcher.isDraggingHorizontally() ? getSize(axis, view) / 2f : 0;
        } else {
            return tabSwitcher.isDraggingHorizontally() ? 0 : getSize(axis, view) / 2f;
        }
    }

    /**
     * Returns the pivot of a view on a specific axis, when it is closed.
     *
     * @param axis
     *         The axis as a value of the enum {@link Axis}. The axis may not be null
     * @param view
     *         The view, whose pivot should be returned, as an instance of the class {@link View}.
     *         The view may not be null
     * @return The pivot of the given view on the given axis as a {@link Float} value
     */
    public final float getPivotWhenClosing(@NonNull final Axis axis, @NonNull final View view) {
        ensureNotNull(axis, "The axis may not be null");
        ensureNotNull(view, "The view may not be null");

        if (axis == Axis.DRAGGING_AXIS) {
            return maxTabSpacing;
        } else {
            return getDefaultPivot(axis, view);
        }
    }

    /**
     * Returns the pivot of a view on a specific axis, when overshooting at the start.
     *
     * @param axis
     *         The axis as a value of the enum {@link Axis}. The axis may not be null
     * @param view
     *         The view, whose pivot should be returned, as an instance of the class {@link View}.
     *         The view may not be null
     * @return The pivot of the given view on the given axis as a {@link Float} value
     */
    public final float getPivotOnOvershootStart(@NonNull final Axis axis,
                                                @NonNull final View view) {
        ensureNotNull(axis, "The axis may not be null");
        ensureNotNull(view, "The view may not be null");

        return getSize(axis, view) / 2f;
    }

    /**
     * Returns the pivot of a view on a specific axis, when overshooting at the end.
     *
     * @param axis
     *         The axis as a value of the enum {@link Axis}. The axis may not be null
     * @param view
     *         The view, whose pivot should be returned, as an instance of the class {@link View}.
     *         The view may not be null
     * @return The pivot of the given view on the given axis as a {@link Float} value
     */
    public final float getPivotOnOvershootEnd(@NonNull final Axis axis, @NonNull final View view) {
        ensureNotNull(axis, "The axis may not be null");
        ensureNotNull(view, "The view may not be null");

        if (axis == Axis.DRAGGING_AXIS) {
            return maxTabSpacing;
        } else {
            return getSize(axis, view) / 2f;
        }
    }

    /**
     * Sets the pivot of a view on a specific axis.
     *
     * @param axis
     *         The axis as a value of the enum {@link Axis}. The axis may not be null
     * @param view
     *         The view, whose pivot should be set, as an instance of the class {@link View}. The
     *         view may not be null
     * @param pivot
     *         The pivot, which should be set, as a {@link Float} value
     */
    public final void setPivot(@NonNull final Axis axis, @NonNull final View view,
                               final float pivot) {
        ensureNotNull(axis, "The axis may not be null");
        ensureNotNull(view, "The view may not be null");
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();

        if (getOrientationInvariantAxis(axis) == Axis.DRAGGING_AXIS) {
            float newPivot = pivot - layoutParams.topMargin - tabTitleContainerHeight;
            view.setTranslationY(view.getTranslationY() +
                    (view.getPivotY() - newPivot) * (1 - view.getScaleY()));
            view.setPivotY(newPivot);
        } else {
            float newPivot = pivot - layoutParams.leftMargin;
            view.setTranslationX(view.getTranslationX() +
                    (view.getPivotX() - newPivot) * (1 - view.getScaleX()));
            view.setPivotX(newPivot);
        }
    }

    /**
     * Returns the rotation of a view on a specific axis.
     *
     * @param axis
     *         The axis as a value of the enum {@link Axis}. The axis may not be null
     * @param view
     *         The view, whose rotation should be returned, as an instance of the class {@link
     *         View}. The view may not be null
     * @return The rotation of the given view on the given axis as a {@link Float} value
     */
    public final float getRotation(@NonNull final Axis axis, @NonNull final View view) {
        ensureNotNull(axis, "The axis may not be null");
        ensureNotNull(view, "The view may not be null");

        if (getOrientationInvariantAxis(axis) == Axis.DRAGGING_AXIS) {
            return view.getRotationY();
        } else {
            return view.getRotationX();
        }
    }

    /**
     * Sets the rotation of a view on a specific axis.
     *
     * @param axis
     *         The axis as a value of the enum {@link Axis}. The axis may not be null
     * @param view
     *         The view, whose rotation should be set, as an instance of the class {@link View}. The
     *         view may not be null
     * @param angle
     *         The rotation, which should be set, as a {@link Float} value
     */
    public final void setRotation(@NonNull final Axis axis, @NonNull final View view,
                                  final float angle) {
        ensureNotNull(axis, "The axis may not be null");
        ensureNotNull(view, "The view may not be null");

        if (getOrientationInvariantAxis(axis) == Axis.DRAGGING_AXIS) {
            view.setRotationY(tabSwitcher.isDraggingHorizontally() ? -1 * angle : angle);
        } else {
            view.setRotationX(tabSwitcher.isDraggingHorizontally() ? -1 * angle : angle);
        }
    }

    /**
     * Animates the rotation of a view on a specific axis.
     *
     * @param axis
     *         The axis as a value of the enum {@link Axis}. The axis may not be null
     * @param animator
     *         The animator, should be used to animate the rotation, as an instance of the class
     *         {@link ViewPropertyAnimator}. The animator may not be null
     * @param angle
     *         The rotation, which should be set by the animation, as a {@link Float} value
     */
    public final void animateRotation(@NonNull final Axis axis,
                                      @NonNull final ViewPropertyAnimator animator,
                                      final float angle) {
        ensureNotNull(axis, "The axis may not be null");
        ensureNotNull(animator, "The animator may not be null");

        if (getOrientationInvariantAxis(axis) == Axis.DRAGGING_AXIS) {
            animator.rotationY(tabSwitcher.isDraggingHorizontally() ? -1 * angle : angle);
        } else {
            animator.rotationX(tabSwitcher.isDraggingHorizontally() ? -1 * angle : angle);
        }
    }

}