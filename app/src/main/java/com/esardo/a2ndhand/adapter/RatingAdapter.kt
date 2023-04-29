package com.esardo.a2ndhand.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.esardo.a2ndhand.R
import com.esardo.a2ndhand.databinding.ItemRatingBinding
import com.esardo.a2ndhand.model.Rating
import com.google.firebase.firestore.FirebaseFirestore

class RatingAdapter(private val ratingList: List<Rating>) : RecyclerView.Adapter<RatingAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(layoutInflater.inflate(R.layout.item_rating, parent, false))
    }

    override fun getItemCount(): Int = ratingList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = ratingList[position]
        holder.bind(item)
    }

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        private val binding = ItemRatingBinding.bind(view)

        // Binds elements to it's value
        private val rbVotes = binding.rbVotes
        private val tvComment = binding.tvComment
        private val tvFrom = binding.tvFrom

        fun bind (rating: Rating) {
            rbVotes.rating = rating.Points.toFloat()
            if(rating.Observations != "") tvComment.text = rating.Observations
            else tvComment.visibility = GONE

            //Obtener el nombre del usuario para mostrarlo en el TextView
            var userName: String? = ""
            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("User").document(rating.From)
            userRef.get().addOnSuccessListener { documentSnapshot ->
                if(documentSnapshot.exists()) {
                    userName = documentSnapshot.getString("User")
                    tvFrom.text = userName
                }
            }
        }
    }
}