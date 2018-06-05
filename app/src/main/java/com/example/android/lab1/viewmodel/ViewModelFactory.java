package com.example.android.lab1.viewmodel;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.android.lab1.model.Book;
import com.example.android.lab1.model.User;
import com.google.firebase.firestore.GeoPoint;

public class ViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final int firstDistance;
    private final int secondDistance;
    private final int homePageLimit;
    /**
     * @params[0] --> bookID
     * @params[1] --> ownerUID
     */

    private String[] params;
    private GeoPoint geoPoint;

    public ViewModelFactory(String...  params) {
        this.params = params;
        firstDistance = 0;
        secondDistance = 0;
        homePageLimit = 0;
    }

    public ViewModelFactory(GeoPoint geoPoint,
                            int firstDistance,
                            int secondDistance,
                            int homePageLimit) {
        this.geoPoint = geoPoint;
        this.firstDistance = firstDistance;
        this.secondDistance = secondDistance;
        this.homePageLimit = homePageLimit;
    }

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
        }else if(modelClass == MessagesViewModel.class){
            return (T) new MessagesViewModel(params[0]);
        }
        else if(modelClass == BooksViewModel.class){
            if(geoPoint != null)
                return (T) new BooksViewModel(geoPoint,
                        firstDistance,
                        secondDistance,
                        homePageLimit);
            else
                return super.create(modelClass);
        } else if (modelClass == YourLibraryViewModel.class) {
            return (T) new YourLibraryViewModel(params[0]); // params[0] --> uid
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
