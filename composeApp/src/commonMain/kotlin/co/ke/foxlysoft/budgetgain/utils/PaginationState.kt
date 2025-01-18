package co.ke.foxlysoft.budgetgain.utils

enum class PaginationState {
    REQUEST_INACTIVE,
    LOADING,
    PAGINATING,
    ERROR,
    PAGINATION_EXHAUST,
    EMPTY,
}