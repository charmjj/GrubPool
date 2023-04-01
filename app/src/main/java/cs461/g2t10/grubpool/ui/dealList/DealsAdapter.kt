package cs461.g2t10.grubpool.ui.dealList

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import cs461.g2t10.grubpool.R
import cs461.g2t10.grubpool.data.api.S3_BASE_URL
import cs461.g2t10.grubpool.data.models.FoodDeal
import cs461.g2t10.grubpool.databinding.DealsItemBinding
import java.text.SimpleDateFormat
import java.util.Date

class DealsAdapter : RecyclerView.Adapter<DealsAdapter.DealViewHolder>() {

    private val dataList = ArrayList<FoodDeal>()
    var listener: ClickListener? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DealViewHolder {
        val itemView = DealsItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return DealViewHolder(itemView, listener)
    }

    override fun onBindViewHolder(holder: DealViewHolder, position: Int) {
        holder.onBind(dataList[position])
    }

    override fun getItemCount(): Int = dataList.size

    fun addItems(list: List<FoodDeal>) {
        dataList.clear()
        dataList.addAll(list)
        notifyDataSetChanged()
    }

    class DealViewHolder(
        private val binding: DealsItemBinding,
        private val listener: ClickListener?
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBind(data: FoodDeal) = with(binding) {
            val context = root.context
            dealName.text = data.name
            dealDescription.text = data.description
            finalPrice.text = context.getString(R.string.dollar_value, ((100 - data.discount) * data.price) / 100)
            originalPrice.text = context.getString(R.string.dollar_value, data.price)
            discount.text = context.getString(R.string.percent_value, data.discount, "%")
            dealTime.text = data.date?.format()
            Glide.with(context).load(S3_BASE_URL.plus(data.imageUrl)).into(cartImage)
            root.setOnClickListener {
                listener?.onClick(data)
            }
        }
    }

    interface ClickListener {
        fun onClick(data: FoodDeal)
    }

}

fun Date?.format(): String {
    return try {
        val sdf = SimpleDateFormat("DD/MM/YYYY")
        sdf.format(this)
    } catch (_: Exception) {
        ""
    }
}
