import axios from 'axios'
const api=axios.create({baseURL:import.meta.env.VITE_API_URL||'/api',timeout:120000,headers:{Accept:'application/json'}})
api.interceptors.response.use(r=>r,e=>Promise.reject(new Error(e.response?.data?.message||e.message||'Não foi possível concluir a operação.')))
export const quizApi={get:(topic,amount=5)=>api.get('/quiz',{params:{topic,amount}}).then(r=>r.data),submit:answers=>api.post('/quiz/submit',answers).then(r=>r.data),random:(amount=20)=>api.get('/random',{params:{amount}}).then(r=>r.data)}
export const materialApi={list:search=>api.get('/materials',{params:{search}}).then(r=>r.data),get:id=>api.get(`/materials/${id}`).then(r=>r.data),upload:(file,onProgress)=>{const form=new FormData();form.append('file',file);return api.post('/materials/upload',form,{onUploadProgress:onProgress}).then(r=>r.data)},addUrl:url=>api.post('/materials/url',{url}).then(r=>r.data),remove:id=>api.delete(`/materials/${id}`),action:(name,payload)=>api.post(`/materials/${name}`,payload).then(r=>r.data),query:question=>api.post('/study/query',{question}).then(r=>r.data)}
export default api
