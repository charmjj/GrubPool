package cs461.g2t10.grubpool.data.models

data class CuisineModel(val cuisine_id: Int, val cuisine: String) {
    override fun toString(): String {
        return cuisine
    }
}