<template>
  <div id="app">
      <GameBoard ref="gameBoard"/>
  </div>
</template>

<script>
import GameBoard from './components/GameBoard.vue'

export default {
  name: 'App',
  components: {
      GameBoard
  }
}
</script>
