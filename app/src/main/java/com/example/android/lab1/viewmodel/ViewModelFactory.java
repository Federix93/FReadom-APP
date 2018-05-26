package com.example.android.lab1.viewmodel;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.example.android.lab1.model.User;

public class ViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    /**
     * @params[0] --> bookID
     * @params[1] --> ownerUID
     */

    private String[] params;

    public ViewModelFactory(String...  params) {
        this.params = params;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if(modelClass == BookViewModel.class){
            return (T) new BookViewModel(params[0]);
        }
        else if(modelClass == OpenedChatViewModel.class){
            return (T) new OpenedChatViewModel(params[0], params[1]);
        }
        else if(modelClass == UserViewModel.class){
            if(params.length > 0)
                return (T) new UserViewModel(params[0]);
            else
                return (T) new UserViewModel();
        }else{
            return super.create(modelClass);
        }
    }
}
