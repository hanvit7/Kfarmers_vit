package com.leadplatform.kfarmers.view.base;

import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;

public abstract class BaseDragListFragment extends BaseListFragment {
    public final boolean REMOVE_ENABLED = false;
    public final boolean SORT_ENABLED = true;
    public final boolean DRAG_ENABLED = true;
    public final int DRAG_INIT_MODE = DragSortController.ON_DOWN;
    public final int REMOVE_MODE = DragSortController.FLING_REMOVE;

    public abstract int getDragHandleId();

    public abstract int getClickRemoveId();

    public DragSortController buildController(DragSortListView dslv) {
        DragSortController controller = new DragSortController(dslv);
        controller.setRemoveEnabled(REMOVE_ENABLED);
        controller.setSortEnabled(SORT_ENABLED);
        controller.setDragInitMode(DRAG_INIT_MODE);
        controller.setRemoveMode(REMOVE_MODE);
        controller.setDragHandleId(getDragHandleId());
        controller.setClickRemoveId(getClickRemoveId());
        return controller;
    }
}
