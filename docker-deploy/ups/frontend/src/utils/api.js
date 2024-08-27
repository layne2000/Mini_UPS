import axios from "axios";
import {Message} from 'element-plus'
import router from "../main"

axios.interceptors.response.use(success =>{
    if(success.status && success.status ==200){
        if (success.data.code==500||success.data.code==401||success.data.code==403){
            Message.error({message:success.data.message});
            return;
        }
        if (success.data){
            Message.success({message:success.data.message});
        }
    }
    return success.data;
},error=>{
    if (error.data.code==504||error.data.code==404){
        Message.error({message:"oops1"});
    } else if (error.data.code==403){
        Message.error({message:"oops2"});
    }else if (error.data.code==404){
        Message.error({message:"oops3"});
        router.replace('/');
    }else{
        if(eror.response.data.message){
            Message.error({message:error.response.data.message});
        }else{
            Message.error({message:"unknown bad"});
        }
    }
    return;
});
export const postRequest=(url,params)=>{
    return axios({
        method:'post',
        url:`${base}${url}`,
        data:params
    })
}