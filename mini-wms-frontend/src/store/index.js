import { createPinia } from 'pinia'
import persistedState from 'pinia-plugin-persistedstate'

const pinia = createPinia()
pinia.use(persistedState)

export default pinia