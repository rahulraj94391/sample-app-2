package com.example.instagram.adapters

//class SelectedPostImageAdapter(Uris: MutableList<Uri>) : RecyclerView.Adapter<SelectedPostImageAdapter.MyViewHolder>() {
//    private lateinit var context: Context
//
//    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
//        context = recyclerView.context
//        super.onAttachedToRecyclerView(recyclerView)
//    }
//
//    private var listOfImageUris: MutableList<Uri>
//
//    init {
//        this.listOfImageUris = Uris
//    }
//
//    inner class MyViewHolder(suggestionList: View) : RecyclerView.ViewHolder(suggestionList) {
//        val image: ImageView = suggestionList.findViewById(R.id.gridImage)
//
//        init {
//            image.setOnLongClickListener {
//                if (listOfImageUris.size < 2) {
//                    Toast.makeText(context, "Post at least one photo.", Toast.LENGTH_SHORT).show()
//                }
//                else {
//                    listOfImageUris.removeAt(adapterPosition)
//                    notifyItemRemoved(adapterPosition)
//                }
//                true
//            }
//        }
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
//        val suggestionList = LayoutInflater.from(parent.context).inflate(R.layout.row_photo_one_grid, parent, false)
//        return MyViewHolder(suggestionList)
//    }
//
//    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
//        holder.image.setImageURI(listOfImageUris[position])
//    }
//
//    override fun getItemCount(): Int {
//        return listOfImageUris.size
//    }
//
//    fun setNewList(newUris: MutableList<Uri>) {
//        listOfImageUris = newUris
//        notifyDataSetChanged()
//    }
//}