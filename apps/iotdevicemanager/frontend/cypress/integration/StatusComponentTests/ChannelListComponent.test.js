describe('ChannelListComponent ', function () {
  it('front page can be opened', function () {
    cy.visit('http://localhost:3000')
    cy.get('.channel-list-container')
    cy.get('.channel-list-left').get('h2').contains('Temperature')
  })
})
