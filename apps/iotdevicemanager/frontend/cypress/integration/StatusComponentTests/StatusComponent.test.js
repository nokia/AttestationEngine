describe('StatusComponent ', function () {
  it('front page can be opened', function () {
    cy.visit('http://localhost:3000')
    cy.get('.status-container')
    cy.get('.status-description').get('p').contains('Trusted')
  })
})
