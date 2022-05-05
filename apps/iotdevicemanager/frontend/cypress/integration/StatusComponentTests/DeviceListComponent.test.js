describe('DeviceListComponent ', function () {
  it('front page can be opened', function () {
    cy.visit('http://localhost:3000')
    cy.get('.device-list-component-container')
    cy.get('.device-list-component-left')
    cy.get('.device-list-component-textcontainer').get('h2')
  })
})
