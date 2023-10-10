package com.example.fashionshoppingadmin

import android.app.Activity
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.fashionshoppingadmin.databinding.ActivityMainBinding
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.util.UUID

class MainActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    var productModel = ProductModel()

    private val startForProfileImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val data = result.data

            when (resultCode) {
                Activity.RESULT_OK -> {
                    //Image Uri will not be null for RESULT_OK
                    binding.root.setBackgroundColor(Color.LTGRAY)
                    binding.mainLayout.visibility = View.GONE
                    binding.spinKit.visibility = View.VISIBLE
                    val fileUri = data?.data!!
                    //store image into cloud storage
                    Firebase.storage.reference.child("Product Image/${UUID.randomUUID()}")
                        .putFile(fileUri)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                // get url from storage
                                it.result.storage.downloadUrl.addOnSuccessListener { url -> // get url
                                    productModel.imageUrl = url.toString()
                                    binding.productImage.setImageURI(fileUri)
                                    binding.productImage.tag = 2
                                    binding.root.setBackgroundColor(Color.WHITE)
                                    binding.mainLayout.visibility = View.VISIBLE
                                    binding.spinKit.visibility = View.GONE
                                }

                            } else {
                                Toast.makeText(this, it.exception?.message, Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }


                }

                ImagePicker.RESULT_ERROR -> {
                    Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
                }

                else -> {
                    Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.productImage.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .createIntent { intent ->
                    startForProfileImageResult.launch(intent)
                }
        }

        binding.btnAddProduct.setOnClickListener {
            val pName = binding.etProductName.text.toString().trim()
            val pAmount = binding.etAmount.text.toString().trim()
            val pDesc = binding.etDesc.text.toString().trim()

            if (pName.isEmpty()) {
                binding.etProductName.error = "Enter Product Name"
            } else if (pAmount.isEmpty()) {
                binding.etAmount.error = "Enter Product Amount"
            } else if (pDesc.isEmpty()) {
                binding.etDesc.error = "Enter Description"
            } else if (binding.productImage.tag.toString() == "1") {
                Toast.makeText(this, "Select product image", Toast.LENGTH_SHORT).show()
            } else {
                productModel.productName = pName
                productModel.price = pAmount.toDouble()
                productModel.desc = pDesc

                //store product data into fire store
                Firebase.firestore.collection("Product Collection")
                    .document(UUID.randomUUID().toString()).set(productModel).addOnCompleteListener {
                        if (it.isSuccessful){
                            binding.etProductName.setText("")
                            binding.etAmount.setText("")
                            binding.etDesc.setText("")
                            binding.productImage.tag = 1
                            binding.productImage.setImageResource(R.drawable.placeholder)
                            Toast.makeText(this, "Added Successfully !!!", Toast.LENGTH_SHORT).show()
                        }else{
                            Toast.makeText(this, it.exception?.message, Toast.LENGTH_SHORT).show()
                        }
                    }
            }


        }
    }
}