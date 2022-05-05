describe('DropDownComponent ', function () {
  it('DropDownComponent contains elements', function () {
    cy.visit('http://localhost:3000')
    cy.get('.dropdown-wrapper')
    cy.get('.dropdown-button').first().click()
    cy.get('.dropdown-list-content-active')
      .get('.dropdown-list-element')
      .first()
      .click()
    cy.get('.dropdown-list-content-active').should('not.exist')
  })
})
