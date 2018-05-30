package com.example.android.lab1.viewmodel;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.example.android.lab1.model.Book;
import com.example.android.lab1.model.User;
import com.google.firebase.firestore.GeoPoint;

public class ViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    /**
     * @params[0] --> bookID
     * @params[1] --> ownerUID
     */

    private String[] params;
    private GeoPoint geoPoint;

    public ViewModelFactory(String...  params) {
        this.params = params;
    }

    public ViewModelFactory(GeoPoint geoPoint) { this.geoPoint = geoPoint; }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if(modelClass == BookViewModel.class){
            return (T) new BookViewModel(params[0]);
        } else if(modelClass == OpenedChatViewModel.class){
            return (T) new OpenedChatViewModel(params[0], params[1]);
        }else if(modelClass == ConversationsViewModel.class){
            return (T) new ConversationsViewModel(params[0]);
        }else if(modelClass == UserRealtimeDBViewModel.class){
            return (T) new UserRealtimeDBViewModel(params[0]);
        }else if(modelClass == BooksViewModel.class){
            if(geoPoint != null)
                return (T) new BooksViewModel(geoPoint);
            if(params.length > 0)
                return (T) new BooksViewModel(params[0]);
            else
                return (T) new BooksViewModel();
        }
        else if(modelClass == UserViewModel.class){
            if(params.length > 0)
                return (T) new UserViewModel(params[0]);
            else
                return (T) new UserViewModel();
        }
        else{
            return super.create(modelClass);
        }
    }
}
