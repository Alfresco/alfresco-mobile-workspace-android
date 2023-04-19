package com.alfresco.content.component

import com.alfresco.content.simpleController

internal fun SearchUserGroupComponentSheet.epoxyController() = simpleController(viewModel) { state ->
    state.listUserGroup.forEach { item ->
        listViewUserRow {
            id(item.id)
            data(item)
            clickListener { model, _, _, _ ->
                onApply?.invoke(model.data())
                dismiss()
            }
        }
    }
}

internal fun SearchUserGroupComponentSheet.executeSearch(term: String) {
    scrollToTop()
    viewModel.setSearchQuery(term)
}

internal fun SearchUserGroupComponentSheet.scrollToTop() {
    if (isResumed) {
        binding.recyclerView.layoutManager?.scrollToPosition(0)
    }
}
