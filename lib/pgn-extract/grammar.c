/*
 *  Program: pgn-extract: a Portable Game Notation (PGN) extractor.
 *  Copyright (C) 1994-2015 David Barnes
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 1, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 *  David Barnes may be contacted as D.J.Barnes@kent.ac.uk
 *  http://www.cs.kent.ac.uk/people/staff/djb/
 *
 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include "bool.h"
#include "mymalloc.h"
#include "defs.h"
#include "typedef.h"
#include "tokens.h"
#include "taglist.h"
#include "lex.h"
#include "moves.h"
#include "map.h"
#include "lists.h"
#include "apply.h"
#include "output.h"
#include "eco.h"
#include "end.h"
#include "grammar.h"
#include "hashing.h"

static TokenType current_symbol = NO_TOKEN;

/* Keep track of which RAV level we are at.
 * This is used to check whether a TERMINATING_RESULT is the final one
 * and whether NULL_MOVEs are allowed.
 */
static unsigned RAV_level = 0;

/* Retain details of the header of a game.
 * This comprises the Tags and any comment prefixing the
 * moves of the game.
 */
static struct{
    /* The tag values. */
    char **Tags;
    unsigned header_tags_length;
    CommentList *prefix_comment;
} GameHeader;

static void ParseOptGameList(SourceFileType file_type);
static Boolean ParseGame(Move **returned_move_list);
Boolean ParseOptTagList(void);
Boolean ParseTag(void);
static Move *ParseMoveList(void);
static Move *ParseMoveAndVariants(void);
static Move *ParseMove(void);
static Move *ParseMoveUnit(void);
static CommentList *ParseOptCommentList(void);
Boolean ParseOptMoveNumber(void);
static StringList *ParseOptNAGList(void);
static Variation *ParseOptVariantList(void);
static Variation *ParseVariant(void);
static char *ParseResult(void);

static void setup_for_new_game(void);
void free_tags(void);
static void check_result(char **Tags,const char *terminating_result);
static void free_comment_list(CommentList *comment_list);
static void DealWithEcoLine(Move *move_list);
static void DealWithGame(Move *move_list);
static Boolean finished_processing(void);

    /* Initialise the game header structure to contain
     * space for the default number of tags.
     * The space will have to be increased if new tags are
     * identified in the program source.
     */
void
init_game_header(void)
{
    unsigned i;
    GameHeader.header_tags_length = ORIGINAL_NUMBER_OF_TAGS;
    GameHeader.Tags = (char **) MallocOrDie(GameHeader.header_tags_length*
                    sizeof(*GameHeader.Tags));
    for(i = 0; i < GameHeader.header_tags_length; i++){
        GameHeader.Tags[i] = (char *) NULL;
    }
}

void
increase_game_header_tags_length(unsigned new_length)
{   unsigned i;

    if(new_length <= GameHeader.header_tags_length){
        fprintf(GlobalState.logfile,
                "Internal error: inappropriate length %d ",new_length);
        fprintf(GlobalState.logfile,
                " passed to increase_game_header_tags().\n");
        exit(1);
    }
    GameHeader.Tags = (char **) ReallocOrDie((void *) GameHeader.Tags,
                                      new_length*sizeof(*GameHeader.Tags));
    for(i = GameHeader.header_tags_length; i < new_length; i++){
        GameHeader.Tags[i] = NULL;
    }
    GameHeader.header_tags_length = new_length;
}

        /* Try to open the given file. Error and exit on failure. */
FILE *
must_open_file(const char *filename,const char *mode)
{   FILE *fp;

    fp = fopen(filename,mode);
    if(fp == NULL){
        fprintf(GlobalState.logfile,"Unable to open the file: \"%s\"\n",
                filename);
        exit(1);
    }
    return fp;
}

        /* Print out on outfp the current details and
         * terminate with a newline.
         */
void
report_details(FILE *outfp)
{
      if(GameHeader.Tags[WHITE_TAG] != NULL){
          fprintf(outfp,"%s - ",GameHeader.Tags[WHITE_TAG]);
      }
      if(GameHeader.Tags[BLACK_TAG] != NULL){
          fprintf(outfp,"%s ",GameHeader.Tags[BLACK_TAG]);
      }

      if(GameHeader.Tags[EVENT_TAG] != NULL){
          fprintf(outfp,"%s ",GameHeader.Tags[EVENT_TAG]);
      }
      if(GameHeader.Tags[SITE_TAG] != NULL){
          fprintf(outfp,"%s ",GameHeader.Tags[SITE_TAG]);
      }

      if(GameHeader.Tags[DATE_TAG] != NULL){
          fprintf(outfp,"%s ",GameHeader.Tags[DATE_TAG]);
      }
      putc('\n',outfp);
      fflush(outfp);
}

        /* Check that terminating_result is consistent with
         * Tags[RESULT_TAG].  If the latter is missing, fill it
         * in from terminating_result.
         */
static void
check_result(char **Tags,const char *terminating_result)
{   char *result_tag = Tags[RESULT_TAG];

    if(terminating_result != NULL){
        if((result_tag == NULL) || (*result_tag == '\0') ||
                    (strcmp(result_tag,"?") == 0)){
            /* Use a copy of terminating result. */
            result_tag = copy_string(terminating_result);
            Tags[RESULT_TAG] = result_tag;
        }
        else if((result_tag != NULL) &&
                    (strcmp(terminating_result,"*") != 0) &&
                    (strcmp(result_tag,terminating_result) != 0)){
            print_error_context(GlobalState.logfile);
            fprintf(GlobalState.logfile,
                        "Inconsistent result strings in the following game.\n");
            report_details(GlobalState.logfile);
        }
        else{
            /* Ok. */
        }
    }
}

        /* Select which file to write to based upon the game state.
         * This will depend upon:
         *        Whether the number of games per file is limited.
         *        Whether ECO_level > DONT_DIVIDE.
         */

static FILE *
select_output_file(StateInfo *GameState,const char *eco)
{   
    if(GameState->games_per_file > 0){
        if(GameState->games_per_file == 1 || (GameState->num_games_matched % GameState->games_per_file) == 1){
              /* Time to open the next one. */
              char filename[50];

              if(GameState->outputfile != NULL){
                  (void) fclose(GameState->outputfile);
              }
              sprintf(filename,"%u%s",
                        GameState->next_file_number,
                        output_file_suffix(GameState->output_format));
              GameState->outputfile = must_open_file(filename,"w");
              GameState->next_file_number++;
        }
    }
    else if(GameState->ECO_level > DONT_DIVIDE){
        /* Open a file of the appropriate name. */
        if(GameState->outputfile != NULL){
            /* @@@ In practice, this might need refinement.
             * Repeated opening and closing may prove inefficient.
             */
            (void) fclose(GameState->outputfile);
            GameState->outputfile = open_eco_output_file(
                                GameState->ECO_level,
                                eco);
        }
    }
    else{
    }
    return GameState->outputfile;
}

  /*
   * Conditions for finishing processing, other than all the input
   * having been processed.
   */
static Boolean finished_processing(void)
{
    return (GlobalState.matching_game_number > 0 &&
	  GlobalState.num_games_matched == GlobalState.matching_game_number);
}

static void
ParseOptGameList(SourceFileType file_type)
{   Move *move_list = NULL;

    while(ParseGame(&move_list) && !finished_processing()){
        if(file_type == NORMALFILE){
            DealWithGame(move_list);
        }
        else if(file_type == CHECKFILE){
            DealWithGame(move_list);
        }
        else if(file_type == ECOFILE){
            if(move_list != NULL){
                DealWithEcoLine(move_list);
            }
            else {
                fprintf(GlobalState.logfile,"ECO line with zero moves.\n");
                report_details(GlobalState.logfile);
            }
        }
        else{
            /* Unknown type. */
            free_tags();
            free_move_list(move_list);
        }
        move_list = NULL;
        setup_for_new_game();
    }
}

        /* Parse a game and return a pointer to any valid list of moves
         * in returned_move_list.
         */
static Boolean
ParseGame(Move **returned_move_list)
{   /* Boolean something_found = FALSE; */
    CommentList *prefix_comment;
    Move *move_list = NULL;
    char *result;
    /* There shouldn't be a hanging comment before the result,
     * but there sometimes is.
     */
    CommentList *hanging_comment;

    /* Assume that we won't return anything. */
    *returned_move_list = NULL;
    /* Skip over any junk between games. */
    current_symbol = skip_to_next_game(current_symbol);
    prefix_comment = ParseOptCommentList();
    if(prefix_comment != NULL){
        /* Free this here, as it is hard to
         * know whether it belongs to the game or the file.
         * It is better to put game comments after the tags.
         */
        /* something_found = TRUE; */
        free_comment_list(prefix_comment);
        prefix_comment = NULL;
    }
    if(ParseOptTagList()){
        /* something_found = TRUE; */
    }
    /* @@@ Beware of comments and/or tags without moves. */
    move_list = ParseMoveList();

    /* @@@ Look for a comment with no move text before the result. */
    hanging_comment = ParseOptCommentList();
    /* Append this to the final move, if there is one. */

    /* Look for a result, even if there were no moves. */
    result = ParseResult();
    if(move_list != NULL){
        /* Find the last move. */
        Move *last_move = move_list;

        while(last_move->next != NULL){
            last_move = last_move->next;
        }
        if(hanging_comment != NULL) {
            append_comments_to_move(last_move,hanging_comment);
        }
        if(result != NULL){
            /* Append it to the last move. */
            last_move->terminating_result = result;
            check_result(GameHeader.Tags,result);
            *returned_move_list = move_list;
        }
        else{
            fprintf(GlobalState.logfile,"Missing result.\n");
            report_details(GlobalState.logfile);
        }
        /* something_found = TRUE; */
    }
    else{
        /* @@@ Nothing to attach the comment to. */
        (void) free((void *) hanging_comment);
        hanging_comment = NULL;
        /*
         * Workaround for games with zero moves.
         * Check the result for consistency with the tags, but then
         * there is no move to attach it to.
         * When outputting a game, the missing result in this case
         * will have to be supplied from the tags.
         */
        check_result(GameHeader.Tags,result);
        if(result != NULL){
            (void) free((void *)result);
        }
        *returned_move_list = NULL;
    }
    return current_symbol != EOF_TOKEN;
}

Boolean
ParseOptTagList(void)
{   Boolean something_found = FALSE;
    CommentList *prefix_comment;
 
    while(ParseTag()){
        something_found = TRUE;
    }
    if(something_found){
        /* Perform any consistency checks. */
        if((GameHeader.Tags[SETUP_TAG] != NULL) &&
                (strcmp(GameHeader.Tags[SETUP_TAG],"1") == 0)){
            /* There must be a FEN_TAG to go with it. */
            if(GameHeader.Tags[FEN_TAG] == NULL){
                 fprintf(GlobalState.logfile,
                        "Missing %s Tag to accompany %s Tag.\n",
                        tag_header_string(FEN_TAG),
                        tag_header_string(SETUP_TAG));
                print_error_context(GlobalState.logfile);
            }
        }
    }
    prefix_comment = ParseOptCommentList();
    if(prefix_comment != NULL){
        GameHeader.prefix_comment = prefix_comment;
        something_found = TRUE;
    }
    return something_found;
}

Boolean
ParseTag(void)
{   Boolean TagFound = TRUE;

    if(current_symbol == TAG){
        TagName tag_index = yylval.tag_index;

        current_symbol = next_token();
        if(current_symbol == STRING){
            char *tag_string = yylval.token_string;

            if(tag_index < GameHeader.header_tags_length){
                GameHeader.Tags[tag_index] = tag_string;
            }
            else{
                print_error_context(GlobalState.logfile);
                fprintf(GlobalState.logfile,
                        "Internal error: Illegal tag index %d for %s\n",
                        tag_index,tag_string);
                exit(1);
            }
            current_symbol = next_token();
        }
        else{
            print_error_context(GlobalState.logfile);
            fprintf(GlobalState.logfile,"Missing tag string.\n");
        }
    }
    else if(current_symbol == STRING){
        print_error_context(GlobalState.logfile);
        fprintf(GlobalState.logfile,"Missing tag for %s.\n",yylval.token_string);
        (void) free((void *)yylval.token_string);
        current_symbol = next_token();
    }
    else{
        TagFound = FALSE;
    }
    return TagFound;
}


static Move *
ParseMoveList(void)
{   Move *head = NULL, *tail = NULL;

    head = ParseMoveAndVariants();
    if(head != NULL){
        Move *next_move;

        tail = head;
        while((next_move = ParseMoveAndVariants()) != NULL){
            tail->next = next_move;
            tail = next_move;
        }
    }
    return head;
}

static Move *
ParseMoveAndVariants(void)
{   Move *move_details;

    move_details = ParseMove();
    if(move_details != NULL){
        CommentList *comment;

        move_details->Variants = ParseOptVariantList();
        comment = ParseOptCommentList();
        if(comment != NULL){
            append_comments_to_move(move_details,comment);
        }
    }
    return move_details;
}


static Move *
ParseMove(void)
{   Move *move_details = NULL;

    if(ParseOptMoveNumber()){
    }
    /* @@@ Watch out for finding just the number. */
    move_details = ParseMoveUnit();
    if(move_details != NULL){
        CommentList *comment;

        move_details->Nags = ParseOptNAGList();
        comment = ParseOptCommentList();
        if(comment != NULL){
            append_comments_to_move(move_details,comment);
        }
    }
    return move_details;
}

static Move *
ParseMoveUnit(void)
{   Move *move_details = NULL;

    if(current_symbol == MOVE){
        move_details = yylval.move_details;

	if(move_details->class == NULL_MOVE && RAV_level == 0) {
            print_error_context(GlobalState.logfile);
	    fprintf(GlobalState.logfile, "Null moves (--) only allowed in variations.\n");
	}

        current_symbol = next_token();
        if(current_symbol == CHECK_SYMBOL){
            strcat((char *) move_details->move,"+");
            current_symbol = next_token();
            /* Sometimes + is followed by #, so cover this case. */
            if(current_symbol == CHECK_SYMBOL){
                current_symbol = next_token();
            }
        }
        move_details->Comment = ParseOptCommentList();
    }
    return move_details;
}

static CommentList *
ParseOptCommentList(void)
{   CommentList *head = NULL, *tail = NULL;

    while(current_symbol == COMMENT){
        if(head == NULL){
            head = tail = yylval.comment;
        }
        else{
            tail->next = yylval.comment;
            tail = tail->next;
        }
        current_symbol = next_token();
    }
    return head;
}

Boolean
ParseOptMoveNumber(void)
{   Boolean something_found = FALSE;

    if(current_symbol == MOVE_NUMBER){
        current_symbol = next_token();
        something_found = TRUE;
    }
    return something_found;
}

static StringList *
ParseOptNAGList(void)
{   StringList *nags = NULL;
 
    while(current_symbol == NAG){
        if(GlobalState.keep_NAGs){
            nags = save_string_list_item(nags,yylval.token_string);
        }
        else{
            (void) free((void *)yylval.token_string);
        }
        current_symbol = next_token();
    }
    return nags;
}

static Variation *
ParseOptVariantList(void)
{   Variation *head = NULL, *tail = NULL,
        *variation;
 
    while((variation = ParseVariant()) != NULL){
        if(head == NULL){
            head = tail = variation;
        }
        else{
            tail->next = variation;
            tail = variation;
        }
    }
    return head;
}

static Variation *
ParseVariant(void)
{   Variation *variation = NULL;

    if(current_symbol == RAV_START){
        CommentList *prefix_comment;
        CommentList *suffix_comment;
        char *result = NULL;
        Move *moves;

        RAV_level++;
        variation = MallocOrDie(sizeof(Variation));

        current_symbol = next_token();
        prefix_comment = ParseOptCommentList();
        if(prefix_comment != NULL){
        }
        moves = ParseMoveList();
        if(moves == NULL){
            print_error_context(GlobalState.logfile);
            fprintf(GlobalState.logfile,"Missing move list in variation.\n");
        }
        result = ParseResult();
        if((result != NULL) && (moves != NULL)){
            /* Find the last move, to which to append the terminating
             * result.
             */
            Move *last_move = moves;
            CommentList *trailing_comment;

            while(last_move->next != NULL){
                last_move = last_move->next;
            }
            last_move->terminating_result = result;
            /* Accept a comment after the result, but it will
             * be printed out preceding the result.
             */
            trailing_comment = ParseOptCommentList();
            if(trailing_comment != NULL){
                append_comments_to_move(last_move,trailing_comment);
            }
        }
        else{
            /* Ok. */
        }
        if(current_symbol == RAV_END){
            RAV_level--;
            current_symbol = next_token();
        }
        else{
            fprintf(GlobalState.logfile,"Missing ')' to close variation.\n");
            print_error_context(GlobalState.logfile);
        }
        suffix_comment = ParseOptCommentList();
        if(suffix_comment != NULL){
        }
        variation->prefix_comment = prefix_comment;
        variation->suffix_comment = suffix_comment;
        variation->moves = moves;
        variation->next = NULL;
    }
    return variation;
}

static char *
ParseResult(void)
{   char *result = NULL;

    if(current_symbol == TERMINATING_RESULT){
        result = yylval.token_string;
        if(RAV_level == 0){
            /* In the interests of skipping any intervening material
             * between games, set the lookahead to a dummy token.
             */
            current_symbol = NO_TOKEN;
        }
        else{
            current_symbol = next_token();
        }
    }
    return result;
}

static void
setup_for_new_game(void)
{
    restart_lex_for_new_game();
    RAV_level = 0;
}

        /* Discard any data held in the GameHeader.Tags structure. */
void
free_tags(void)
{   unsigned tag;

    for(tag = 0; tag < GameHeader.header_tags_length; tag++){
        if(GameHeader.Tags[tag] != NULL){
            free(GameHeader.Tags[tag]);
            GameHeader.Tags[tag] = NULL;
        }
    }
}

        /* Discard data from a gathered game. */
void
free_string_list(StringList *list)
{   StringList *next;

    while(list != NULL){
        next = list;
        list = list->next;
        if(next->str != NULL){
            (void) free((void *)next->str);
        }
        (void) free((void *)next);
    }
}

static void
free_comment_list(CommentList *comment_list)
{
    while(comment_list != NULL){
        CommentList *this_comment = comment_list;

        if(comment_list->Comment != NULL){
            free_string_list(comment_list->Comment);
        }
        comment_list = comment_list->next;
        (void) free((void *)this_comment);
    }
}

static void
free_variation(Variation *variation)
{   Variation *next;

    while(variation != NULL){
        next = variation;
        variation = variation->next;
        if(next->prefix_comment != NULL){
            free_comment_list(next->prefix_comment);
        }
        if(next->suffix_comment != NULL){
            free_comment_list(next->suffix_comment);
        }
        if(next->moves != NULL){
            (void) free_move_list((void *)next->moves);
        }
        (void) free((void *)next);
    }
}

void
free_move_list(Move *move_list)
{   Move *next;

    while(move_list != NULL){
        next = move_list;
        move_list = move_list->next;
        if(next->Nags != NULL){
            free_string_list(next->Nags);
        }
        if(next->Comment != NULL){
            free_comment_list(next->Comment);
        }
        if(next->Variants != NULL){
            free_variation(next->Variants);
        }
        if(next->epd != NULL){
            (void) free((void *)next->epd);
        }
        if(next->terminating_result != NULL){
            (void) free((void *)next->terminating_result);
        }
        (void) free((void *)next);
    }
}

        /* Add str onto the tail of list and
         * return the head of the resulting list.
         */
StringList *
save_string_list_item(StringList *list,const char *str)
{
    if(str != NULL){
      StringList *new_item;

      new_item = (StringList *)MallocOrDie(sizeof(*new_item));
      new_item->str = str;
      new_item->next = NULL;
      if(list == NULL){
          list = new_item;
      }
      else{
          StringList *tail = list;

          while(tail->next != NULL){
              tail = tail->next;
          }
          tail->next = new_item;
      }
    }
    return list;
}

        /* Append any comments in Comment onto the end of
         * any associated with move.
         */
void
append_comments_to_move(Move *move,CommentList *Comment)
{
    if(Comment != NULL){
        /* Add in to the end of any already existing. */
        if(move->Comment == NULL){
            move->Comment = Comment;
        }
        else{
            /* Add in the final comment to 
             * the end of any existing for this move.
             */
            CommentList *tail = move->Comment;

            while(tail->next != NULL){
                tail = tail->next;
            }
            tail->next = Comment;
        }
    }
}

static void
DealWithGame(Move *move_list)
{   Game current_game;
    /* Record whether the game has been printed or not.
     * This is used for the case of the -n flag which catches
     * all non-printed games.
     */
    Boolean game_output = FALSE;
    /* We need a dummy argument for apply_move_list. */
    unsigned plycount;

    /* Update the count of how many games handled. */
    GlobalState.num_games_processed++;

    /* Fill in the information currently known. */
    current_game.tags = GameHeader.Tags;
    current_game.tags_length = GameHeader.header_tags_length;
    current_game.prefix_comment = GameHeader.prefix_comment;
    current_game.moves = move_list;
    current_game.moves_checked = FALSE;
    current_game.moves_ok = FALSE;
    current_game.error_ply = 0;
  
    /* Determine whether or not this game is wanted, on the
     * basis of the various selection criteria available.
     */
    
    /*
     * apply_move_list checks out the moves.
     * If it returns TRUE as a match, it will also fill in the
     *                 current_game.final_hash_value and
     *                 current_game.cumulative_hash_value
     * fields of current_game so that these can be used in the
     * previous_occurance function.
     *
     * If there are any tag criteria, it will be easy to quickly
     * eliminate most games without going through the length
     * process of game matching.
     *
     * If ECO adding is done, the order of checking may cause
     * a conflict here since it won't be possible to reject a game
     * based on its ECO code unless it already has one.
     * Therefore, Check for the ECO tag only after everything else has
     * been checked.
     */
    if(CheckTagDetailsNotECO(current_game.tags,current_game.tags_length) &&
             apply_move_list(&current_game,&plycount) && 
             check_move_bounds(plycount) &&
             check_textual_variations(current_game) &&
             check_for_ending(current_game.moves) &&
             check_for_only_checkmate(current_game.moves) &&
             CheckECOTag(current_game.tags)){
        /* If there is no original filename then the game is not a
         * duplicate.
         */
        const char *original_filename = previous_occurance(current_game, plycount);

        if((original_filename == NULL) && GlobalState.suppress_originals){
            /* Don't output first occurrences. */
        }
        else if((original_filename == NULL) || !GlobalState.suppress_duplicates){
	    GlobalState.num_games_matched++;
            if(GlobalState.check_only) {
	        /* We are only checking. */
		if(GlobalState.verbose){
		    /* Report progress on logfile. */
		    report_details(GlobalState.logfile);
		}
	    }
	    else if(GlobalState.current_file_type == CHECKFILE){
		/* We are only checking, so don't count this as a matched game. */
		GlobalState.num_games_matched--;
	    }
	    else if(GlobalState.matching_game_number > 0 &&
		  GlobalState.num_games_matched != GlobalState.matching_game_number) {
	        /* This is not the right matching game to be output. */
	    }
	    else {
                /* This game is to be kept and output. */
                FILE *outputfile = select_output_file(&GlobalState,
                        current_game.tags[ECO_TAG]);

                /* See if we wish to separate out duplicates. */
                if((original_filename != NULL) &&
                        (GlobalState.duplicate_file != NULL)){
                    static const char *last_input_file = NULL;
  
                    outputfile = GlobalState.duplicate_file;
                    if((last_input_file != GlobalState.current_input_file) &&
                       (GlobalState.current_input_file != NULL)){
                           /* Record which file this and succeeding
                            * duplicates come from.
                            */
                          print_str(outputfile,"{ From: ");
                          print_str(outputfile,
                                      GlobalState.current_input_file);
                          print_str(outputfile," }");
                          terminate_line(outputfile);
                          last_input_file = GlobalState.current_input_file;
                    }
                  print_str(outputfile,"{ First found in: ");
                  print_str(outputfile,original_filename);
                  print_str(outputfile," }");
                  terminate_line(outputfile);
                }
		/* Now output what we have. */
		output_game(current_game,outputfile);
		game_output = TRUE;
		if(GlobalState.verbose){
		    /* Report progress on logfile. */
		    report_details(GlobalState.logfile);
		}
	    }
        }
    }
    if(!game_output && (GlobalState.non_matching_file != NULL) &&
    		GlobalState.current_file_type != CHECKFILE){
        /* The user wants to keep everything else. */
        if(!current_game.moves_checked){
             /* Make sure that the move text is in a reasonable state. */
             (void) apply_move_list(&current_game,&plycount);
        }
        if(current_game.moves_ok || GlobalState.keep_broken_games){
            output_game(current_game,GlobalState.non_matching_file);
        }
    }
  
    /* Game is finished with, so free everything. */
    if(GameHeader.prefix_comment != NULL){
        free_comment_list(GameHeader.prefix_comment);
    }
    /* Ensure that the GameHeader's prefix comment is NULL for
     * the next game.
     */
    GameHeader.prefix_comment = NULL;
  
    free_tags();
    free_move_list(current_game.moves);
    if((GlobalState.num_games_processed % 10) == 0){
        fprintf(stderr,"Games: %lu\r",GlobalState.num_games_processed);
    }
}

static void
DealWithEcoLine(Move *move_list)
{   Game current_game;
    /* We need to know the length of a game to store with the
     * hash information as a sanity check.
     */
    unsigned number_of_half_moves;

    /* Fill in the information currently known. */
    current_game.tags = GameHeader.Tags;
    current_game.tags_length = GameHeader.header_tags_length;
    current_game.prefix_comment = GameHeader.prefix_comment;
    current_game.moves = move_list;
    current_game.moves_checked = FALSE;
    current_game.moves_ok = FALSE;
    current_game.error_ply = 0;
  
    /* apply_eco_move_list checks out the moves.
     * It will also fill in the
     *                 current_game.final_hash_value and
     *                 current_game.cumulative_hash_value
     * fields of current_game.
     */
    if(apply_eco_move_list(&current_game,&number_of_half_moves)){
        if(current_game.moves_ok){
            /* Store the ECO code in the appropriate hash location. */
            save_eco_details(current_game,number_of_half_moves);
        }
    }
  
    /* Game is finished with, so free everything. */
    if(GameHeader.prefix_comment != NULL){
        free_comment_list(GameHeader.prefix_comment);
    }
    /* Ensure that the GameHeader's prefix comment is NULL for
     * the next game.
     */
    GameHeader.prefix_comment = NULL;
  
    free_tags();
    free_move_list(current_game.moves);
}

        /* If file_type == ECOFILE we are dealing with a file of ECO
         * input rather than a normal game file.
         */
int
yyparse(SourceFileType file_type)
{
    setup_for_new_game();
    current_symbol = skip_to_next_game(NO_TOKEN);
    ParseOptGameList(file_type);
    if(current_symbol == EOF_TOKEN){
        /* Ok -- EOF. */
        return 0;
    }
    else if(finished_processing()) {
        /* Ok -- done all we need to. */
	return 0;
    }
    else{
        fprintf(GlobalState.logfile,"End of input reached before end of file.\n");
        return 1;
    }
}

