/*
 * Copyright (C) 2018 Drake, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.drake.brv.listener

import android.graphics.Canvas
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.drake.brv.BindingAdapter
import com.drake.brv.item.ItemTouchable
import java.util.*

/**
 * 默认实现拖拽替换和侧滑删除
 */
open class DefaultItemTouchCallback(var adapter: BindingAdapter) : ItemTouchHelper.Callback() {


    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val layoutPosition = viewHolder.layoutPosition
        adapter.notifyItemRemoved(layoutPosition)
        (adapter.models as ArrayList).removeAt(layoutPosition)
    }

    override fun getMovementFlags(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
    ): Int {

        var drag = 0
        var swipe = 0

        if (viewHolder is BindingAdapter.BindingViewHolder) {
            val model = viewHolder.getModel<Any>()

            if (model is ItemTouchable) {
                drag = model.itemDrag
                swipe = model.itemSwipe
            }
        }

        return makeMovementFlags(drag, swipe)
    }


    override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
    ) {

        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {

            val swipeView = viewHolder.itemView.findViewWithTag<View>("swipe")

            if (swipeView != null) {
                swipeView.translationX = dX
            } else {
                super.onChildDraw(
                        c,
                        recyclerView,
                        viewHolder,
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive
                )
            }
        } else {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
    }

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        return 1f
    }

    /**
     * 拖拽替换成功
     */
    open fun onDrag(
            source: BindingAdapter.BindingViewHolder,
            target: BindingAdapter.BindingViewHolder
    ) {

    }

    override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
    ): Boolean {

        val currentPosition = recyclerView.getChildLayoutPosition(viewHolder.itemView)
        val targetPosition = recyclerView.getChildLayoutPosition(target.itemView)

        if (target is BindingAdapter.BindingViewHolder) {

            val model = target.getModel<Any>()
            if (model is ItemTouchable && model.itemDrag != 0) {
                adapter.notifyItemMoved(currentPosition, targetPosition)
                Collections.swap(
                        adapter.models,
                        currentPosition - adapter.headerCount,
                        targetPosition - adapter.headerCount
                )

                onDrag(viewHolder as BindingAdapter.BindingViewHolder, target)
            }
        }
        return false
    }
}