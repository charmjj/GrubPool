package cs461.g2t10.grubpool.data.models

data class RestrictionModel(val restriction_id: Int, val restriction: String){
    override fun toString(): String {
        return restriction
    }
}