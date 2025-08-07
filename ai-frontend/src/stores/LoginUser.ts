import {defineStore} from 'pinia'
import {ref} from "vue";
import {getLoginUser} from "@/api/userController.ts";

export const useLoginUserStore = defineStore('loginUser', () => {
  const loginUser = ref<API.LoginUserVO>({
    userName: '未登录',
  })

  async function fetchLoginUser(){
    const user = await getLoginUser();
    if( user.data.code==0 && user.data.data ){
      loginUser.value = user.data.data;
    }
  }

  function setLoginUser(newLoginUser: any){
    loginUser.value = newLoginUser;
  }

  return{ loginUser, fetchLoginUser, setLoginUser }
})
