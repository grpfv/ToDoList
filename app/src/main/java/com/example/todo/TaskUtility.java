package com.example.todo;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class TaskUtility {

    static CollectionReference getCollectionReferenceForToDo(){
        //FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        //return FirebaseFirestore.getInstance().collection("Courses").document(get.currentUser.getUid()).collection("my_Courses")
        return FirebaseFirestore.getInstance().collection("ToDoList");
    }
}
